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

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import osmproxy.elements.OSMLight;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;
import routing.RouteInfo;
import smartcity.MasterAgent;
import smartcity.buses.BrigadeInfo;
import smartcity.buses.BusInfo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    public static List<OSMLight> getLights(Document xmlDocument) {
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
                        info.addLightOsmId(item.getAttributes().getNamedItem("id").getNodeValue());
                    }
                }
            }
        }
        return info;
    }

    private static void parseBusInfo(Map<String, BrigadeInfo> brigadeNrToBrigadeInfo, OSMStation station, JSONObject jsonObject) {
        JSONArray msg = (JSONArray) jsonObject.get("result");
        String currentBrigadeNr = "";
        for (Object o : msg) {
            JSONObject values = (JSONObject) o;
            JSONArray valuesArray = (JSONArray) values.get("values");
            for (Object item : valuesArray) {
                JSONObject valueObject = (JSONObject) item;
                String key = (String) valueObject.get("key");
                String value = (String) valueObject.get("value");
                if (key.equals("brygada")) {
                    currentBrigadeNr = value;
                    if (!brigadeNrToBrigadeInfo.containsKey(value)) {
                        brigadeNrToBrigadeInfo.put(value, new BrigadeInfo(value));
                    }
                }
                else if (key.equals("czas")) {
                    brigadeNrToBrigadeInfo.get(currentBrigadeNr).addToTimetable(station.getId(), value);
                }
            }
        }
    }

    /**
     * @param query the overpass query
     * @return the nodes in the formulated query
     */
    public static Document getNodesViaOverpass(String query) throws IOException, ParserConfigurationException, SAXException {
        String hostname = OVERPASS_API;
        String queryString = query; // readFileAsString(query);

        URL osm = new URL(hostname);
        HttpURLConnection connection = (HttpURLConnection) osm.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
        printout.writeBytes("data=" + URLEncoder.encode(queryString, StandardCharsets.UTF_8));
        printout.flush();
        printout.close();

        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        return docBuilder.parse(connection.getInputStream());
    }

    public static JSONObject getNodesViaWarszawskie(String query) {
        URL nieOsm;
        Scanner scanner;
        StringBuilder json = new StringBuilder();
        JSONObject jObject = null;
        try {
            nieOsm = new URL(query);
            scanner = new Scanner(nieOsm.openStream());
            while (scanner.hasNext()) {
                json.append(scanner.nextLine());
            }
            jObject = (JSONObject) jsonParser.parse(json.toString());
        } catch (Exception e) {
            logger.warn("Error trying to get 'Warszawskie busy'", e);
        }
        return jObject;
    }

    public static List<OSMLight> sendFullTrafficSignalQuery(List<Long> osmWayIds) {
        List<OSMLight> LightNodes = new ArrayList<>();
        try {
            var overpassNodes = getNodesViaOverpass(getFullTrafficSignalQuery(osmWayIds));
            LightNodes = getLights(overpassNodes);
        } catch (Exception e) {
            logger.error("Error trying to get light nodes", e);
        }
        return LightNodes;
    }

    public static RouteInfo sendMultipleWayAndItsNodesQuery(List<Long> osmWayIds) {
        RouteInfo info = null;
        try {
            var overpassNodes = getNodesViaOverpass(getMultipleWayAndItsNodesQuery(osmWayIds));
            info = parseWayAndNodes(overpassNodes);
        } catch (Exception e) {
            logger.warn("Error trying to get route info", e);
        }
        return info;
    }

    private static String getFullTrafficSignalQuery(List<Long> osmWayIds) {
        StringBuilder builder = new StringBuilder();
        builder.append("<osm-script>");
        for (long id : osmWayIds) {
            builder.append(getSingleTrafficSignalQuery(id));
        }
        builder.append("</osm-script>");
        return builder.toString();
    }

    private static String getMultipleWayAndItsNodesQuery(List<Long> osmWayIds) {
        StringBuilder builder = new StringBuilder();
        builder.append("<osm-script>");
        for (long id : osmWayIds) {
            builder.append(getSingleWayAndItsNodesQuery(id));
        }
        builder.append("</osm-script>");
        return builder.toString();
    }

    private static String getSingleWayAndItsNodesQuery(long osmWayId) {
        return "<id-query type=\"way\" ref=\"" + osmWayId + "\" into=\"minor\"/>\r\n" +
                "  <item from=\"minor\" into=\"_\"/>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"full\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" +
                "  <recurse from=\"minor\" type=\"way-node\"/>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"tags\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n";
    }

    private static String getSingleTrafficSignalQuery(long osmWayId) {
        return "<osm-script>\r\n" +
                "  <id-query type=\"way\" ref=\"" + osmWayId + "\" into=\"minor\"/>\r\n" +
                "  <query into=\"_\" type=\"node\">\r\n" +
                "    <has-kv k=\"highway\" modv=\"\" v=\"traffic_signals\"/>\r\n" +
                "    <recurse from=\"minor\" type=\"way-node\"/>\r\n" +
                "  </query>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" +
                "  <id-query type=\"way\" ref=\"" + osmWayId + "\"/>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" +
                "</osm-script>";
    }

    public static void prepareLightManagersInRadiusAndLightIdToLightManagerIdHashSet(GeoPosition middlePoint, int radius) {
        Document xmlDocument = getXmlDocument(CROSSROADS);
        Node osmRoot = xmlDocument.getFirstChild();
        NodeList districtXMLNodes = osmRoot.getChildNodes();
        for (int i = 0; i < districtXMLNodes.getLength(); i++) {
            if (districtXMLNodes.item(i).getNodeName().equals("district")) {
                addAllDesiredIdsInDistrict(districtXMLNodes.item(i), middlePoint, radius);
            }
        }

    }

    public static List<OSMNode> parseLightNodeList(Document nodesViaOverpass) {
        List<OSMNode> lightNodeList = new ArrayList<>();
        Node osmRoot = nodesViaOverpass.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            parseLightNode(lightNodeList, osmXMLNodes.item(i));
        }
        return lightNodeList;
    }

    private static void parseLightNode(List<OSMNode> lightNodeList, Node item) {
        if (item.getNodeName().equals("node")) {
            final OSMNode nodeWithParents = new OSMNode(item.getAttributes());
            lightNodeList.add(nodeWithParents);
        }
        else if (item.getNodeName().equals("way")) {
            final OSMWay osmWay = new OSMWay(item);
            lightNodeList.get(lightNodeList.size() - 1).addParentWay(osmWay);
        }
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

    public static Set<BusInfo> getBusInfo(int radius, double middleLat, double middleLon) {
        logger.info("STEP 2/" + MasterAgent.STEPS + ": Sending bus overpass query");
        Set<BusInfo> infoSet = sendBusOverpassQuery(radius, middleLat, middleLon);
        logger.info("STEP 4/" + MasterAgent.STEPS + ": Starting warszawskie query and parsing");
        int i = 0;
        for (BusInfo info : infoSet) {
            logger.info("STEP 4/" + MasterAgent.STEPS + " (SUBSTEP " + (++i) + "/" + infoSet.size() + "): Warszawskie query sending & parsing substep");
            sendBusWarszawskieQuery(info);
        }
        return infoSet;
    }

    private static Set<BusInfo> sendBusOverpassQuery(int radius, double middleLat, double middleLon) {
        Set<BusInfo> infoSet = null;
        try {
            var overpassQuery = getBusOverpassQuery(radius, middleLat, middleLon);
            var overpassInfo = getNodesViaOverpass(overpassQuery);
            infoSet = parseBusInfo(overpassInfo, radius, middleLat, middleLon);
        } catch (Exception e) {
            logger.warn("Error getting bus info.", e);
        }

        return infoSet;
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
                            appendSingleBusWayOverpassQuery(builder, Long.parseLong(namedItemID.getNodeValue()));
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
                    var overpassNodes = getNodesViaOverpass(getBusWayOverpassQueryWithPayload(builder));
                    var osmWay = parseOsmWay(overpassNodes, radius, middleLat, middleLon);
                    info.setRoute(osmWay);
                } catch (Exception e) {
                    logger.warn("Error setting osm way", e);
                }
            }
            if (item.getNodeName().equals("node")) {
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

    private static List<OSMWay> parseOsmWay(Document nodesViaOverpass, int radius, double middleLat, double middleLon) {
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
                    adjacentNodeRef = way.determineRelationOrientation(adjacentNodeRef);
                }

                // TODO: CORRECT POTENTIAL BUGS CAUSING ROUTE TO BE CUT INTO PIECES BECAUSE OF RZĄŻEWSKI CASE
                if (way.startsInCircle(radius, middleLat, middleLon)) {
                    route.add(way);
                }
            }
        }
        return route;
    }

    private static Pair<OSMWay, String> determineInitialWayRelOrientation(final NodeList osmXMLNodes) {
        List<OSMWay> twoFirstWays = new ArrayList<>();
        OSMWay way = null;
        int i = 0;
        while (twoFirstWays.size() < 2) {
            Node item = osmXMLNodes.item(++i);
            if (item.getNodeName().equals("way")) {
                way = new OSMWay(item);
                twoFirstWays.add(way);
            }
        }
        return Pair.with(way, twoFirstWays.get(0).determineRelationOrientation(twoFirstWays.get(1)));
    }

    private static void appendSingleBusWayOverpassQuery(StringBuilder query, long osmWayId) {
        query.append("<id-query type=\"way\" ref=\"" + osmWayId + "\"/>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"full\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>");
    }

    private static String getBusWayOverpassQueryWithPayload(StringBuilder query) {
        return "<osm-script>\r\n" +
                query.toString() +
                "</osm-script>";
    }

    private static String getBusOverpassQuery(int radius, double middleLat, double middleLon) {
        return "<osm-script>\r\n" +
                "  <query into=\"_\" type=\"relation\">\r\n" +
                "    <has-kv k=\"route\" modv=\"\" v=\"bus\"/>\r\n" +
                "    <around radius=\"" + radius + "\" lat=\"" + middleLat + "\" lon=\"" + middleLon + "\"/>\r\n" +
                "  </query>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" +
                "  <recurse type=\"relation-node\"/>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" +
                "</osm-script>";
    }

    private static void sendBusWarszawskieQuery(BusInfo info) {
        Map<String, BrigadeInfo> brigadeNrToBrigadeInfo = new HashMap<>();
        for (OSMStation station : info.getStations()) {
            try {
                parseBusInfo(brigadeNrToBrigadeInfo, station, getNodesViaWarszawskie(getBusWarszawskieQuery(station.getBusStopId(), station.getBusStopNr(), info.getBusLine())));
            } catch (NullPointerException e) {
                logger.warn("You shall not pass! (Null.", e);
            }
        }
        info.setBrigadeList(brigadeNrToBrigadeInfo.values());
    }

    private static String getBusWarszawskieQuery(String busStopId, String busStopNr, String busLine) {
        return "https://api.um.warszawa.pl/api/action/dbtimetable_get/?id=e923fa0e-d96c-43f9-ae6e-60518c9f3238&busstopId=" + busStopId + "&busstopNr=" + busStopNr + "&line=" + busLine + "&apikey=400dacf8-9cc4-4d6c-82cc-88d9311401a5";
    }
}
