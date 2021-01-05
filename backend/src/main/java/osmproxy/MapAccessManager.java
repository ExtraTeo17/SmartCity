/*
  (c) Jens Kbler
  This software is public domain
  <p>
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
package osmproxy;

import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.elements.OSMLight;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import routing.RouteInfo;
import routing.core.IZone;
import routing.core.Position;
import utilities.IterableNodeList;
import utilities.NumericHelper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MapAccessManager implements IMapAccessManager {
    private static final Logger logger = LoggerFactory.getLogger(MapAccessManager.class);
    private static final String OVERPASS_API = "https://lz4.overpass-api.de/api/interpreter";
    private static final String ALTERNATE_OVERPASS_API_1 = "https://z.overpass-api.de/api/interpreter";
    private static final String ALTERNATE_OVERPASS_API_2 = "https://overpass.kumi.systems/api/interpreter";
    private static String CURRENT_API = OVERPASS_API;
    private static final String CROSSROADS_LOCATIONS_PATH = "config/crossroads.xml";

    private final DocumentBuilderFactory xmlBuilderFactory;

    public MapAccessManager() {
        this.xmlBuilderFactory = DocumentBuilderFactory.newInstance();
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


    @Override
    public Optional<Document> getNodesDocument(String query) {
        var connection = sendRequest(query);
        try {
            var responseCode = connection.getResponseCode();
            if (responseCode == 429 || responseCode == 504) {
                logger.warn("Current API: " + CURRENT_API + " is overloaded with our requests.");
                CURRENT_API = CURRENT_API.equals(ALTERNATE_OVERPASS_API_1) ? ALTERNATE_OVERPASS_API_2 :
                        CURRENT_API.equals(ALTERNATE_OVERPASS_API_2) ? OVERPASS_API :
                                ALTERNATE_OVERPASS_API_1;
                logger.info("Switching to " + CURRENT_API);
                connection = sendRequest(query);
            }
        } catch (IOException e) {
            logger.error("Error getting response code from connection", e);
            return Optional.empty();
        }

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

    private static HttpURLConnection sendRequest(String query) {
        return sendRequest(CURRENT_API, query);
    }

    private static HttpURLConnection sendRequest(String apiAddress, String query) {
        HttpURLConnection connection = getConnection(apiAddress);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try {
            DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
            printout.writeBytes("data=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
            printout.flush();
            printout.close();
        } catch (IOException e) {
            logger.error("Error sending data to connection", e);
            throw new RuntimeException(e);
        }

        return connection;
    }

    private static HttpURLConnection getConnection(String apiAddress) {
        URL url;
        try {
            url = new URL(apiAddress);
        } catch (MalformedURLException e) {
            logger.error("Error creating url: " + apiAddress);
            throw new RuntimeException(e);
        }

        try {
            return (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            logger.error("Error opening connection to " + apiAddress);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<OSMLight> getOsmLights(List<Long> osmWayIds) {
        var query = OsmQueryManager.getFullTrafficSignalQuery(osmWayIds);
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

     public Optional<RouteInfo> getRouteInfo(List<Long> osmWayIds, boolean notPedestrian) {
        var query = OsmQueryManager.getMultipleWayAndItsNodesQuery(osmWayIds);
        var overpassNodes = getNodesDocument(query);
        if (overpassNodes.isEmpty()) {
            return Optional.empty();
        }
        RouteInfo info;
        try {
            info = parseWayAndNodes(overpassNodes.get(), notPedestrian);
        } catch (Exception e) {
            logger.warn("Error trying to get route info", e);
            return Optional.empty();
        }

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
}
