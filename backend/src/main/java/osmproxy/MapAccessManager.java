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

import jade.core.NotFoundException;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.json.simple.parser.JSONParser;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import osmproxy.buses.BusInfo;
import osmproxy.elements.OSMLight;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;
import routing.RouteInfo;
import smartcity.MasterAgent;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

//import org.osm.lights.diff.OSMNode;
//import org.osm.lights.upload.BasicAuthenticator;

/**
 *
 */
public class MapAccessManager {
    private static final Logger logger = LoggerFactory.getLogger(MapAccessManager.class);
    private static final JSONParser jsonParser = new JSONParser();
    private static final String OVERPASS_API = "https://lz4.overpass-api.de/api/interpreter";
    private static final String CROSSROADS = "config/crossroads.xml";

    /**
     * @return a list of openseamap nodes extracted from xml
     */
    @SuppressWarnings("nls")
    public static List<OSMNode> getNodes(Document xmlDocument) {
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

    /**
     * @return (id, lat, lng)
     */
    private static Triplet<String, String, String> getNodeArgs(Node xmlNode) {
        NamedNodeMap attributes = xmlNode.getAttributes();
        String id = attributes.getNamedItem("id").getNodeValue();
        String latitude = attributes.getNamedItem("lat").getNodeValue();
        String longitude = attributes.getNamedItem("lon").getNodeValue();

        return Triplet.with(id, latitude, longitude);
    }

    private static List<OSMLight> getLights(Document xmlDocument) {
        List<OSMLight> osmLights = new ArrayList<>();
        Node osmRoot = xmlDocument.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        List<OSMNode> nodesOfOneWay = new ArrayList<>();
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            parseLightNode(osmXMLNodes.item(i), osmLights, nodesOfOneWay);
        }
        return osmLights;
    }

    private static void parseLightNode(Node item, List<OSMLight> osmLights, List<OSMNode> nodesOfOneWay) {
        String id, latitude, longitude, adherentWayId;
        if (item.getNodeName().equals("node")) {
            NamedNodeMap attributes = item.getAttributes();
            Node namedItemID = attributes.getNamedItem("id");
            Node namedItemLat = attributes.getNamedItem("lat");
            Node namedItemLon = attributes.getNamedItem("lon");
            id = namedItemID.getNodeValue();
            latitude = namedItemLat.getNodeValue();
            longitude = namedItemLon.getNodeValue();
            nodesOfOneWay.add(new OSMNode(id, latitude, longitude));
        }
        else if (item.getNodeName().equals("way")) {
            NamedNodeMap attributes = item.getAttributes();
            Node namedItemID = attributes.getNamedItem("id");
            adherentWayId = namedItemID.getNodeValue();
            addLightNodeSeries(osmLights, nodesOfOneWay, adherentWayId);
        }
    }

    private static void addLightNodeSeries(List<OSMLight> osmLights, List<OSMNode> nodesOfOneWay,
                                           String adherentWayId) {
        for (OSMNode osmNode : nodesOfOneWay) {
            osmLights.add(new OSMLight(osmNode, adherentWayId));
        }
    }

    private static RouteInfo parseWayAndNodes(Document nodesViaOverpass) {
        try {
            DOMSource source = new DOMSource(nodesViaOverpass);
            File file = new File("output.xml");
            if (file.createNewFile()) {
                FileWriter writer = new FileWriter(file);
                StreamResult result = new StreamResult(writer);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.transform(source, result);
            }
        } catch (Exception e) {
            logger.warn("Could not write to file");
        }

        final RouteInfo info = new RouteInfo();
        Node osmRoot = nodesViaOverpass.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("way")) {
                info.addWay(new OSMWay(item));
            }
            else if (item.getNodeName().equals("node")) { // TODO: for further future: add support for rare way-traffic-signal-crossings cases
                NodeList nodeChildren = item.getChildNodes();
                for (int j = 0; j < nodeChildren.getLength(); ++j) {
                    Node nodeChild = nodeChildren.item(j);
                    if (nodeChild.getNodeName().equals("tag") &&
                            nodeChild.getAttributes().getNamedItem("k").getNodeValue().equals("crossing") &&
                            nodeChild.getAttributes().getNamedItem("v").getNodeValue().equals("traffic_signals")) {
                        info.add(item.getAttributes().getNamedItem("id").getNodeValue());
                    }
                }
            }
        }
        return info;
    }

    public static Set<BusInfo> sendBusOverpassQuery(int radius, double middleLat, double middleLon) {
        Set<BusInfo> infoSet = null;
        var overpassQuery = OsmQueryManager.getBusOverpassQuery(radius, middleLat, middleLon);
        try {
            var overpassInfo = getNodesViaOverpass(overpassQuery);
            infoSet = parseBusInfo(overpassInfo, radius, middleLat, middleLon);
        } catch (Exception e) {
            logger.warn("Error getting bus info.", e);
            throw new RuntimeException(e);
        }

        return infoSet;
    }


    /**
     * @param query the overpass query
     * @return the nodes in the formulated query
     */
    public static Document getNodesViaOverpass(String query) throws IOException, ParserConfigurationException, SAXException {
        URL osm = new URL(OVERPASS_API);
        HttpURLConnection connection = (HttpURLConnection) osm.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
        printout.writeBytes("data=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
        printout.flush();
        printout.close();

        // TODO: Cache builder
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        return docBuilder.parse(connection.getInputStream());
    }

    private static Set<BusInfo> parseBusInfo(Document nodesViaOverpass, int radius, double middleLat, double middleLon) {
        logger.info("STEP 3/" + MasterAgent.STEPS + ": Starting overpass bus info parsing");
        Set<BusInfo> infoSet = new LinkedHashSet<>();
        Node osmRoot = nodesViaOverpass.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("relation")) {
                BusInfo info = new BusInfo();
                infoSet.add(info);
                NodeList member_list = item.getChildNodes();
                StringBuilder builder = new StringBuilder();
                for (int j = 0; j < member_list.getLength(); j++) {
                    Node member = member_list.item(j);
                    if (member.getNodeName().equals("member")) {
                        NamedNodeMap attributes = member.getAttributes();
                        Node namedItemID = attributes.getNamedItem("ref");
                        if (attributes.getNamedItem("role").getNodeValue().contains("stop") && attributes.getNamedItem("type").getNodeValue().equals("node")) {
                            info.addStation(namedItemID.getNodeValue());
                        }
                        else if (attributes.getNamedItem("role").getNodeValue().length() == 0 && attributes.getNamedItem("type").getNodeValue().equals("way")) {
                            long id = Long.parseLong(namedItemID.getNodeValue());
                            builder.append(OsmQueryManager.getSingleBusWayOverpassQuery(id));
                        }
                    }
                    else if (member.getNodeName().equals("tag")) {
                        NamedNodeMap attributes = member.getAttributes();
                        Node namedItemID = attributes.getNamedItem("k");
                        if (namedItemID.getNodeValue().equals("ref")) {
                            Node number_of_line = attributes.getNamedItem("v");
                            info.setBusLine(number_of_line.getNodeValue());
                        }
                    }
                }

                try {
                    var overpassNodes = getNodesViaOverpass(OsmQueryManager.getQueryWithPayload(builder.toString()));
                    var osmWays = parseOsmWay(overpassNodes, radius, middleLat, middleLon);
                    info.setRoute(osmWays);
                } catch (NotFoundException | UnsupportedOperationException e) {
                    logger.warn("Please change the zone, this one is not supported yet.", e);
                    throw new IllegalArgumentException(e);
                } catch (Exception e) {
                    logger.error("Error setting osm way", e);
                    throw new IllegalArgumentException(e);
                }
            }
            else if (item.getNodeName().equals("node")) {
                NamedNodeMap attributes = item.getAttributes();

                String osmId = attributes.getNamedItem("id").getNodeValue();
                String lat = attributes.getNamedItem("lat").getNodeValue();
                String lon = attributes.getNamedItem("lon").getNodeValue();

                if (belongsToCircle(Double.parseDouble(lat), Double.parseDouble(lon), new GeoPosition(middleLat, middleLon), radius) &&
                        !MasterAgent.osmIdToStationOSMNode.containsKey(Long.parseLong(osmId))) {
                    NodeList list_tags = item.getChildNodes();
                    for (int z = 0; z < list_tags.getLength(); z++) {
                        Node tag = list_tags.item(z);
                        if (tag.getNodeName().equals("tag")) {
                            NamedNodeMap attr = tag.getAttributes();
                            Node kAttr = attr.getNamedItem("k");
                            if (kAttr.getNodeValue().equals("public_transport")) {
                                Node vAttr = attr.getNamedItem("v");
                                if (!vAttr.getNodeValue().contains("stop")) {
                                    break;
                                }
                            }
                            else if (kAttr.getNodeValue().equals("ref")) {
                                Node number_of_station = attr.getNamedItem("v");
                                OSMStation stationOSMNode = new OSMStation(osmId, lat, lon, number_of_station.getNodeValue());
                                var agent = MasterAgent.tryAddNewStationAgent(stationOSMNode);
                                agent.start();
                            }
                        }
                    }
                }
            }
        }

        for (BusInfo info : infoSet) {
            info.filterStationsByCircle(middleLat, middleLon, radius);
        }

        return infoSet;
    }

    public static List<OSMLight> sendFullTrafficSignalQuery(List<Long> osmWayIds) {
        List<OSMLight> lightNodes = new ArrayList<>();
        try {
            var overpassNodes = getNodesViaOverpass(OsmQueryManager.getFullTrafficSignalQuery(osmWayIds));
            lightNodes = getLights(overpassNodes);
        } catch (Exception e) {
            logger.error("Error trying to get light nodes", e);
        }
        return lightNodes;
    }

    public static RouteInfo sendMultipleWayAndItsNodesQuery(List<Long> osmWayIds) {
        RouteInfo info = null;
        try {
            var overpassNodes = getNodesViaOverpass(OsmQueryManager.getMultipleWayAndItsNodesQuery(osmWayIds));
            info = parseWayAndNodes(overpassNodes);
        } catch (Exception e) {
            logger.warn("Error trying to get route info", e);
        }
        return info;
    }

    public static boolean prepareLightManagersInRadiusAndLightIdToLightManagerIdHashSet(GeoPosition middlePoint, int radius) {
        try {
            Document xmlDocument = getXmlDocument(CROSSROADS);
            Node osmRoot = xmlDocument.getFirstChild();
            NodeList districtXMLNodes = osmRoot.getChildNodes();
            for (int i = 0; i < districtXMLNodes.getLength(); i++) {
                if (districtXMLNodes.item(i).getNodeName().equals("district")) {
                    addAllDesiredIdsInDistrict(districtXMLNodes.item(i), middlePoint, radius);
                }
            }
        } catch (Exception e) {
            logger.warn("Error preparing light managers", e);
            return false;
        }

        return true;
    }

    public static void parseChildNodesOfWays(Document childNodesOfWays, List<OSMNode> lightsOfTypeA) {
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

    private static void addAllDesiredIdsInDistrict(Node districtRoot, GeoPosition middlePoint, int radius) {
        Node crossroadsRoot = districtRoot.getChildNodes().item(1);
        NodeList crossroadXMLNodes = crossroadsRoot.getChildNodes();
        for (int i = 0; i < crossroadXMLNodes.getLength(); ++i) {
            if (crossroadXMLNodes.item(i).getNodeName().equals("crossroad")) {
                addCrossroadIdIfDesired(crossroadXMLNodes.item(i), middlePoint, radius);
            }
        }

    }

    private static void addCrossroadIdIfDesired(Node crossroad, GeoPosition middlePoint, int radius) {
        Pair<Double, Double> crossroadLatLon = calculateLatLonBasedOnInternalLights(crossroad);

        if (belongsToCircle(crossroadLatLon.getValue0(), crossroadLatLon.getValue1(), middlePoint, radius)) {
            MasterAgent.tryCreateLightManager(crossroad);
        }

    }

    private static Pair<Double, Double> calculateLatLonBasedOnInternalLights(Node crossroad) {
        List<Double> latList = getParametersFromGroup(getCrossroadGroup(crossroad, 1),
                getCrossroadGroup(crossroad, 3), "lat");
        List<Double> lonList = getParametersFromGroup(getCrossroadGroup(crossroad, 1),
                getCrossroadGroup(crossroad, 3), "lon");
        double latAverage = calculateAverage(latList);
        double lonAverage = calculateAverage(lonList);
        return Pair.with(latAverage, lonAverage);
    }

    private static List<Double> getParametersFromGroup(Node group1, Node group2, String parameterName) {
        List<Double> parameterList = new ArrayList<>();
        addLightParametersFromGroup(parameterList, group1, parameterName);
        addLightParametersFromGroup(parameterList, group2, parameterName);
        return parameterList;
    }

    private static void addLightParametersFromGroup(List<Double> list, Node group, String parameterName) {
        NodeList lightNodes = group.getChildNodes();
        for (int i = 0; i < lightNodes.getLength(); ++i) {
            if (lightNodes.item(i).getNodeName().equals("light")) {
                list.add(Double.parseDouble(lightNodes.item(i).getAttributes().getNamedItem(parameterName).getNodeValue()));
            }
        }
    }

    private static double calculateAverage(List<Double> doubleList) {
        double sum = 0;
        for (double value : doubleList) {
            sum += value;
        }
        return sum / (double) (doubleList.size());
    }

    public static Node getCrossroadGroup(Node crossroad, int index) {
        return crossroad.getChildNodes().item(index);
    }

    public static boolean belongsToCircle(double latToBelong, double lonToBelong, GeoPosition middlePoint, int radius) {
        return (((latToBelong - middlePoint.getLatitude()) * (latToBelong - middlePoint.getLatitude()))
                + ((lonToBelong - middlePoint.getLongitude()) * (lonToBelong - middlePoint.getLongitude())))
                < (radius * radius) * 0.0000089 * 0.0000089;
    }

    private static Document getXmlDocument(String filepath) {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
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

    private static List<OSMWay> parseOsmWay(Document nodesViaOverpass, int radius, double middleLat, double middleLon)
            throws NotFoundException {
        List<OSMWay> route = new ArrayList<>();
        Node osmRoot = nodesViaOverpass.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        Pair<OSMWay, String> wayAdjacentNodeRef = determineInitialWayRelOrientation(osmXMLNodes);
        String adjacentNodeRef = wayAdjacentNodeRef.getValue1();
        boolean isFirst = true;
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("way")) {
                OSMWay way;
                if (isFirst) {
                    way = wayAdjacentNodeRef.getValue0();
                    isFirst = false;
                }
                else {
                    way = new OSMWay(item);
                    try {
                        adjacentNodeRef = way.determineRelationOrientation(adjacentNodeRef);
                    } catch (UnsupportedOperationException e) {
                        logger.warn("Failed to determine orientation", e);
                        break;
                    }
                }

                // TODO: CORRECT POTENTIAL BUGS CAUSING ROUTE TO BE CUT INTO PIECES BECAUSE OF RZĄŻEWSKI CASE
                if (way.startsInCircle(radius, middleLat, middleLon)) {
                    route.add(way);
                }
            }
        }
        return route;
    }

    // TODO: Is it returning orientation of next way or current way?
    private static Pair<OSMWay, String> determineInitialWayRelOrientation(final NodeList osmXMLNodes)
            throws NotFoundException {
        OSMWay firstWay = null;
        OSMWay lastWay = null;
        for (int it = 1; it < osmXMLNodes.getLength(); ++it) {
            Node node = osmXMLNodes.item(it);
            if (node.getNodeName().equals("way")) {
                var way = new OSMWay(node);
                if (firstWay == null) {
                    firstWay = way;
                }
                else {
                    lastWay = way;
                    break;
                }
            }
        }

        if (firstWay != null && lastWay != null) {
            var orientation = firstWay.determineRelationOrientation(lastWay);
            return Pair.with(lastWay, orientation);
        }

        throw new NotFoundException("Did not find two 'way'-type nodes in provided list.");
    }
}
