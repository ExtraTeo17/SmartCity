package osmproxy;
/*
  (c) Jens Kübler
  This software is public domain

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import org.apache.commons.io.IOUtils;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import osmproxy.abstractions.ICacheWrapper;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.abstractions.IOverpassApiManager;
import osmproxy.elements.OSMLight;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import osmproxy.elements.data.SimulationData;
import osmproxy.elements.data.WayWithLights;
import osmproxy.utilities.ProgressBar;
import routing.RouteInfo;
import routing.core.IZone;
import routing.core.Position;
import smartcity.config.StaticConfig;
import utilities.IterableNodeList;
import utilities.NumericHelper;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MapAccessManager implements IMapAccessManager {
    private static final Logger logger = LoggerFactory.getLogger(MapAccessManager.class);
    private static final String CROSSROADS_LOCATIONS_PATH = "crossroads.xml";

    private final DocumentBuilderFactory xmlBuilderFactory;
    private final IOverpassApiManager manager;
    private SimulationData simulationData;

    @Inject
    public MapAccessManager(IOverpassApiManager overpassApiManager) {
        this.xmlBuilderFactory = DocumentBuilderFactory.newInstance();
        this.manager = overpassApiManager;
    }


    @Override
    @SuppressWarnings("nls")
    public List<OSMNode> parseNodes(Document xmlDocument) {
        List<OSMNode> osmNodes = new ArrayList<>();
        Node osmRoot = xmlDocument.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("node")) {
                var args = getNodeArgs(item);
                osmNodes.add(new OSMNode(args.getValue0(), args.getValue1(), args.getValue2()));
            }
        }
        return osmNodes;
    }


    private static Triplet<String, String, String> getNodeArgs(Node xmlNode) {
        NamedNodeMap attributes = xmlNode.getAttributes();
        String id = attributes.getNamedItem("id").getNodeValue();
        String latitude = attributes.getNamedItem("lat").getNodeValue();
        String longitude = attributes.getNamedItem("lon").getNodeValue();

        return Triplet.with(id, latitude, longitude);
    }


    @SuppressWarnings("unused")
    private void printStream(final InputStream stream) {
        try {
            logger.info(IOUtils.toString(stream));
            stream.reset();
        } catch (IOException e) {
            logger.warn("Exception while printing stream: " + e);
        }
    }

    /**
     * @param query the overpass query
     * @return the nodes in the formulated query
     */
    @Override
    public Optional<Document> getNodesDocument(String query) {
        var connectionOpt = manager.sendRequest(query);
        if (connectionOpt.isEmpty()) {
            return Optional.empty();
        }

        var connection = connectionOpt.get();
        Document result;
        try {
            var xmlBuilder = xmlBuilderFactory.newDocumentBuilder();
            var stream = connection.getInputStream();
            result = xmlBuilder.parse(stream);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            logger.error("Error parsing data from connection", e);
            return Optional.empty();
        }

        return Optional.of(result);
    }

    @Override
    public List<OSMLight> getOsmLights(List<Long> osmWayIds) {
        var query = OverpassQueryManager.getFullTrafficSignalQuery(osmWayIds);
        var overpassNodes = getNodesDocument(query);
        if (overpassNodes.isEmpty()) {
            return new ArrayList<>();
        }

        List<OSMLight> lightNodes;
        try {
            lightNodes = parseLights(overpassNodes.get());
        } catch (Exception e) {
            logger.error("Error trying to get light nodes", e);
            return new ArrayList<>();
        }

        return lightNodes;
    }

    private static List<OSMLight> parseLights(Document xmlDocument) {
        Node osmRoot = xmlDocument.getFirstChild();
        IterableNodeList osmXMLNodes = IterableNodeList.of(osmRoot.getChildNodes());
        List<OSMLight> osmLights = new ArrayList<>();
        int firstNodeIndex = 0;
        for (var xmlNode : osmXMLNodes) {
            NamedNodeMap attributes = xmlNode.getAttributes();
            if (xmlNode.getNodeName().equals("node")) {
                String id = attributes.getNamedItem("id").getNodeValue();
                String lat = attributes.getNamedItem("lat").getNodeValue();
                String lon = attributes.getNamedItem("lon").getNodeValue();
                osmLights.add(new OSMLight(id, lat, lon));
            }
            else if (xmlNode.getNodeName().equals("way")) {
                String adherentWayId = attributes.getNamedItem("id").getNodeValue();
                for (int i = firstNodeIndex; i < osmLights.size(); ++i) {
                    osmLights.get(i).setAdherentWayId(adherentWayId);
                }
                firstNodeIndex = osmLights.size();
            }
        }

        return osmLights;
    }

    @Override
    public Optional<RouteInfo> getRouteInfo(List<Long> osmWayIds, boolean isNotPedestrian) {
	    if (StaticConfig.USE_SIMULATION_CACHE) {
	    	var optionalInfo = retrieveRouteInfoFromCache(osmWayIds, isNotPedestrian);
	    	if (optionalInfo.isPresent()) {
	    		return optionalInfo;
	    	}
	    }

    	RouteInfo info;
        var query = OverpassQueryManager.getMultipleWayAndItsNodesQuery(osmWayIds);
        var overpassNodes = getNodesDocument(query);
        if (overpassNodes.isEmpty()) {
            return Optional.empty();
        }
        try {
            info = parseWayAndNodes(overpassNodes.get(), isNotPedestrian);
        } catch (Exception e) {
            logger.warn("Error trying to get route info", e);
            return Optional.empty();
        }

        return Optional.of(info);
    }

    private Optional<RouteInfo> retrieveRouteInfoFromCache(List<Long> osmWayIds, boolean isNotPedestrian) {
    	RouteInfo info = new RouteInfo();
		for (long id : osmWayIds) {
			if (!simulationData.contains(id)) {
				return Optional.empty();
			}
			WayWithLights wayWithLights = simulationData.get(id);
			info.addWay(new OSMWay(wayWithLights.getWay()));
			var lightIds = isNotPedestrian ? wayWithLights.getHighwayLightIds() : wayWithLights.getCrossingLightIds();
			for (Long light : lightIds) {
				info.add(light);
			}
		}
		logger.debug("Successfully created route with cache instead of using Overpass API");
		return Optional.of(info);
	}

	private static RouteInfo parseWayAndNodes(Document nodesViaOverpass, boolean notPedestrian) {
        final RouteInfo info = new RouteInfo();
        final String tagType = notPedestrian ? "highway" : "crossing";
        Node osmRoot = nodesViaOverpass.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("way")) {
                info.addWay(new OSMWay(item));
            }
            // TODO: for further future: add support for rare way-traffic-signal-crossings cases
            else if (item.getNodeName().equals("node")) {
                NodeList nodeChildren = item.getChildNodes();
                for (int j = 0; j < nodeChildren.getLength(); ++j) {
                    Node nodeChild = nodeChildren.item(j);
                    if (nodeChild.getNodeName().equals("tag") &&
                            nodeChild.getAttributes().getNamedItem("k").getNodeValue().equals(tagType) &&
                            nodeChild.getAttributes().getNamedItem("v").getNodeValue().equals("traffic_signals")) {
                        var id = Long.parseLong(item.getAttributes().getNamedItem("id").getNodeValue());
                        info.add(id);
                    }
                }
            }
        }
        return info;
    }

    @Override
    @Deprecated
    public List<Node> getLightManagersNodes(IZone zone) {
        var lightManagersNodes = new ArrayList<Node>();
        Document xmlDocument = getXmlDocument(CROSSROADS_LOCATIONS_PATH);
        Node osmRoot = xmlDocument.getFirstChild();
        var districtXMLNodes = IterableNodeList.of(osmRoot.getChildNodes());
        try {
            for (var districtNode : districtXMLNodes) {
                if (districtNode.getNodeName().equals("district")) {
                    Node crossroadsRoot = districtNode.getChildNodes().item(1);
                    var crossroadXMLNodes = IterableNodeList.of(crossroadsRoot.getChildNodes());
                    for (var crossroad : crossroadXMLNodes) {
                        if (crossroad.getNodeName().equals("crossroad")) {
                            var crossroadPos = calculateLatLonBasedOnInternalLights(crossroad);
                            if (zone.contains(crossroadPos)) {
                                lightManagersNodes.add(crossroad);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error preparing light managers", e);
            return lightManagersNodes;
        }

        return lightManagersNodes;
    }

    @Override
    public void parseChildNodesOfWays(Document childNodesOfWays, List<OSMNode> lightsOfTypeA) {
        Node osmRoot = childNodesOfWays.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        int lightIndex = 0, parentWayIndex = 0;
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            final Node item = osmXMLNodes.item(i);
            switch (item.getNodeName()) {
                case "node" -> {
                    String id = item.getAttributes().getNamedItem("id").getNodeValue();
                    lightsOfTypeA.get(lightIndex).addChildNodeIdForParentWay(parentWayIndex, id);
                }
                case "relation" -> ++parentWayIndex;
                case "way" -> {
                    ++lightIndex;
                    parentWayIndex = 0;
                }
            }
        }
    }

    @Override
    public Position calculateLatLonBasedOnInternalLights(Node crossroad) {
        var crossroadA = getCrossroadGroup(crossroad, 1);
        var crossroadB = getCrossroadGroup(crossroad, 3);
        List<Double> latList = getParametersFromGroup(crossroadA, crossroadB, "lat");
        List<Double> lonList = getParametersFromGroup(crossroadA, crossroadB, "lon");
        double latAverage = NumericHelper.calculateAverage(latList);
        double lonAverage = NumericHelper.calculateAverage(lonList);
        return Position.of(latAverage, lonAverage);
    }

    private static List<Double> getParametersFromGroup(Node group1, Node group2, String parameterName) {
        List<Double> parameterList = new ArrayList<>(getLightParametersFromGroup(group1, parameterName));
        parameterList.addAll(getLightParametersFromGroup(group2, parameterName));
        return parameterList;
    }

    private static List<Double> getLightParametersFromGroup(Node group, String parameterName) {
        return IterableNodeList.of(group.getChildNodes()).stream()
                .filter(item -> item.getNodeName().equals("light"))
                .map(item -> Double.parseDouble(item.getAttributes().getNamedItem(parameterName).getNodeName()))
                .collect(Collectors.toList());
    }

    private Node getCrossroadGroup(Node crossroad, int index) {
        return crossroad.getChildNodes().item(index);
    }

    private Document getXmlDocument(String filepath) {
        Document document = null;
        try {
            DocumentBuilder docBuilder = xmlBuilderFactory.newDocumentBuilder();
            document = docBuilder.parse(new File(filepath));
        } catch (SAXException e) {
            logger.warn("Error parsing xml.", e);
        } catch (IOException e) {
            logger.warn("Error accessing file.", e);
        } catch (ParserConfigurationException e) {
            logger.warn("Wrong parser configuration.", e);
        }

        return document;
    }

	@Override
	public void initializeWayCache(IZone zone, ICacheWrapper wrapper) {
		var cachedData = wrapper.getSimulationData();
		if (cachedData.isPresent()) {
			simulationData = cachedData.get();
			return;
		}

		simulationData = new SimulationData();
	    logger.info("Simulation caching enabled, start parsing current zone");

		var wayIdsQuery = OverpassQueryManager.getWaysQuery(zone.getCenter().getLat(),
		        zone.getCenter().getLng(), zone.getRadius());
		var osmWayIdsXmlDoc = getNodesDocument(wayIdsQuery);
		if (osmWayIdsXmlDoc.isEmpty()) {
		    logger.debug("Could not create XML document with way ids for parsing (simulation cache)");
		    return;
		}
		List<Long> osmWayIdsInRadius = parseOsmWayIds(osmWayIdsXmlDoc.get());
		var waysWithNodesQuerySplit = OverpassQueryManager.getMultipleWayAndItsNodesQuerySplit(osmWayIdsInRadius);

		int queryNumber = 1;
		for (String query : waysWithNodesQuerySplit) {
			logger.info("Parsing query " + queryNumber++ + "/" + waysWithNodesQuerySplit.size());
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
			double timeLeftFactor = 0.0002;
			timeLeft = new AtomicInteger((int) (timeLeftFactor * Math.PI * zone.getRadius() * zone.getRadius()));
	        executorService.scheduleAtFixedRate(MapAccessManager::displayProgress, 0, 1, TimeUnit.SECONDS);
			var waysWithNodesXmlDoc = getNodesDocument(query);
			if (waysWithNodesXmlDoc.isEmpty()) {
				logger.debug("Could not create XML document with ways and its nodes for parsing (simulation cache)");
				return;
			}
			executorService.shutdown();
			parseWaysWithLightsAndFillWayCache(waysWithNodesXmlDoc.get());
		}

        logger.info("Cache parsed zone data to file");
		wrapper.cacheData(simulationData);
        logger.info("Simulation data caching finished successfully");
	}

	private static AtomicInteger timeLeft;
	private final static int DISPLAY_PROGRESS_DELTA = 5;

	private static void displayProgress() {
		int progress = timeLeft.getAndDecrement();
		if (progress > 0 && progress % DISPLAY_PROGRESS_DELTA == 0) {
			logger.info("Estimated completion time: less than " + progress + " seconds");
		}
	}

    private List<Long> parseOsmWayIds(Document xmlDoc) {
        List<Long> osmWayIdsInRadius = new ArrayList<>();
        Node osmRoot = xmlDoc.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        for (int i = 0; i < osmXMLNodes.getLength(); i++) {
            Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("way")) {
                osmWayIdsInRadius.add(Long.parseLong(item.getAttributes().getNamedItem("id").getNodeValue()));
            }
        }
        return osmWayIdsInRadius;
    }

	private void parseWaysWithLightsAndFillWayCache(Document nodesViaOverpass) {
        Node osmRoot = nodesViaOverpass.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        WayWithLights wayWithLights = null;
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("way")) {
            	wayWithLights = new WayWithLights();
                wayWithLights.addWay(new OSMWay(item));
                simulationData.put(Long.parseLong(item.getAttributes().getNamedItem("id").getNodeValue()), wayWithLights);
            } else if (item.getNodeName().equals("node")) {
                NodeList nodeChildren = item.getChildNodes();
                for (int j = 0; j < nodeChildren.getLength(); ++j) {
                    Node nodeChild = nodeChildren.item(j);
                    if (nodeChild.getNodeName().equals("tag") &&
                            nodeChild.getAttributes().getNamedItem("k").getNodeValue().equals("highway") &&
                            nodeChild.getAttributes().getNamedItem("v").getNodeValue().equals("traffic_signals")) {
                        wayWithLights.addHighwayLight(Long.parseLong(item.getAttributes().getNamedItem("id").getNodeValue()));
                    } else if (nodeChild.getNodeName().equals("tag") &&
                            nodeChild.getAttributes().getNamedItem("k").getNodeValue().equals("crossing") &&
                            nodeChild.getAttributes().getNamedItem("v").getNodeValue().equals("traffic_signals")) {
                    	wayWithLights.addCrossingLight(Long.parseLong(item.getAttributes().getNamedItem("id").getNodeValue()));
                    }
                }
            }
        }
    }
}
