package osmproxy;

import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import smartcity.MainContainerAgent;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LightAccessManager extends MapAccessManager {

    public static void prepareLightManagersInRadiusAndLightIdToLightManagerIdHashSetBeta(MainContainerAgent smartCityAgent,
                                                                                         GeoPosition middlePoint, int radius) throws IOException, ParserConfigurationException, SAXException {
        List<OSMNode> lightsAround = sendLightsAroundOverpassQueryBeta(radius, middlePoint.getLatitude(), middlePoint.getLongitude());
        List<OSMNode> lightNodeList = sendParentWaysOfLightOverpassQueryBeta(lightsAround);
        List<OSMNode> lightsOfTypeA = filterByTypeA(lightNodeList);
        prepareLightManagers(lightsOfTypeA);
    }

    private static List<OSMNode> sendLightsAroundOverpassQueryBeta(int radius, double middleLat, double middleLon) throws IOException, ParserConfigurationException, SAXException {
        var lightsQuery = getLightsAroundOverpassQueryBeta(radius, middleLat, middleLon);
        var nodes = MapAccessManager.getNodesViaOverpass(lightsQuery);
        List<OSMNode> lightNodes = MapAccessManager.getNodes(nodes);
        return lightNodes;
    }

    private static String getLightsAroundOverpassQueryBeta(int radius, double lat, double lon) {
        return "<osm-script>\r\n" +
                "  <query into=\"_\" type=\"node\">\r\n" +
                "    <has-kv k=\"highway\" modv=\"\" v=\"traffic_signals\"/>\r\n" +
                "    <around radius=\"" + radius + "\" lat=\"" + lat + "\" lon=\"" + lon + "\"/>\r\n" +
                "  </query>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" +
                "</osm-script>";
    }

    private static List<OSMNode> sendParentWaysOfLightOverpassQueryBeta(final List<OSMNode> lightsAround)
            throws IOException, ParserConfigurationException, SAXException {
        final List<OSMNode> lightInfoList = parseLightNodeList(MapAccessManager.getNodesViaOverpass(getParentWaysOfLightOverpassQueryBeta(lightsAround)));
        return lightInfoList;
    }

    private static String getParentWaysOfLightOverpassQueryBeta(final List<OSMNode> lightsAround) {
        StringBuilder builder = new StringBuilder();
        builder.append("<osm-script>");
        for (final OSMNode light : lightsAround) {
            builder.append(getSingleParentWaysOfLightOverpassQueryBeta(light.getId()));
        }
        builder.append("</osm-script>");
        return builder.toString();
    }

    private static String getSingleParentWaysOfLightOverpassQueryBeta(final long osmLightId) {
        return "<id-query type=\"node\" ref=\"" + osmLightId + "\" into=\"crossroad\"/>\r\n" +
                "  <union into=\"_\">\r\n" +
                "    <item from=\"crossroad\" into=\"_\"/>\r\n" +
                "    <recurse from=\"crossroad\" type=\"node-way\"/>\r\n" +
                "  </union>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"full\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>";
    }

    private static List<OSMNode> parseLightNodeList(Document nodesViaOverpass) {
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
            final OSMNode listLastLight = lightNodeList.get(lightNodeList.size() - 1);
            if (osmWay.isOneWayAndLightContiguous(listLastLight.getId())) {
                listLastLight.addParentWay(osmWay);
            }
        }
    }

    private static List<OSMNode> filterByTypeA(List<OSMNode> lightNodeList) {
        final List<OSMNode> typeAlightNodeList = new ArrayList<>();
        for (final OSMNode light : lightNodeList) {
            if (light.isTypeA()) {
                typeAlightNodeList.add(light);
            }
        }
        return typeAlightNodeList;
    }

    private static void fillChildNodesOfWays(final List<OSMNode> lightsOfTypeA)
            throws IOException, ParserConfigurationException, SAXException {
        parseChildNodesOfWays(MapAccessManager.getNodesViaOverpass(getChildNodesOfWaysOverpassQueryBeta(lightsOfTypeA)), lightsOfTypeA);
    }

    private static String getChildNodesOfWaysOverpassQueryBeta(final List<OSMNode> lightsOfTypeA) {
        StringBuilder builder = new StringBuilder();
        builder.append("<osm-script>");
        for (int i = 0; i < lightsOfTypeA.size(); ++i) {
            builder.append(getSingleChildNodesOfWaysOverpassQueryBeta(lightsOfTypeA.get(i).getParentWayIds()));
        }
        builder.append("</osm-script>");
        return builder.toString();
    }

    private static String getSingleChildNodesOfWaysOverpassQueryBeta(final List<Long> parentWayIds) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parentWayIds.size(); ++i) {
            builder.append(getSingleChildNodesOfSingleWayOverpassQueryBeta(parentWayIds.get(i)));
        }
        builder.append(getSingleWayDelimiterOverpassQueryBeta());
        return builder.toString();
    }

    private static Object getSingleWayDelimiterOverpassQueryBeta() {
        return "<id-query type=\"way\" ref=\"" + MapAccessManager.DELIMITER_WAY + "\"/>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"ids_only\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n";
    }

    private static String getSingleChildNodesOfSingleWayOverpassQueryBeta(final long osmWayId) {
        return "<id-query type=\"way\" ref=\"" + osmWayId + "\" into=\"crossroadEntrance\"/>\r\n" +
                "  <union into=\"_\">\r\n" +
                "    <recurse from=\"crossroadEntrance\" type=\"way-node\"/>\r\n" +
                "    <id-query type=\"relation\" ref=\"" + MapAccessManager.DELIMITER_RELATION + "\"/>\r\n" +
                "  </union>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"ids_only\" n=\"\" order=\"id\" s=\"\" w=\"\"/>";
    }

    private static void parseChildNodesOfWays(Document childNodesOfWays, List<OSMNode> lightsOfTypeA) {
        Node osmRoot = childNodesOfWays.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        int lightIndex = 0, parentWayIndex = 0;
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            final Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("node")) {
                lightsOfTypeA.get(lightIndex).addChildNodeIdForParentWay(parentWayIndex,
                        item.getAttributes().getNamedItem("id").getNodeValue());
            }
            else if (item.getNodeName().equals("relation")) {
                ++parentWayIndex;
            }
            else if (item.getNodeName().equals("way")) {
                ++lightIndex;
                parentWayIndex = 0;
            }
        }
    }

    private static void prepareLightManagers(List<OSMNode> lightsOfTypeA) {
        for (final OSMNode centerCrossroadNode : lightsOfTypeA) {
            if (centerCrossroadNode.determineParentOrientationsTowardsCrossroad()) {
                MainContainerAgent.tryAddNewLightManagerAgent(centerCrossroadNode);
            }
        }
    }
}
