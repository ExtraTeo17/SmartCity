package osmproxy;

import com.google.common.annotations.Beta;
import org.jxmapviewer.viewer.GeoPosition;
import org.xml.sax.SAXException;
import osmproxy.elements.OSMNode;
import smartcity.MasterAgent;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Beta
public class LightAccessManager extends MapAccessManager {

    public static void constructLightManagers(GeoPosition middlePoint, int radius)
            throws IOException, ParserConfigurationException, SAXException {
        List<OSMNode> lightsAround = getLightNodesAround(radius, middlePoint.getLatitude(), middlePoint.getLongitude());
        List<OSMNode> lightNodeList = sendParentWaysOfLightOverpassQuery(lightsAround);
        List<OSMNode> lightsOfTypeA = lightNodeList.stream().filter(OSMNode::isTypeA).collect(Collectors.toList());
        createLightManagers(lightsOfTypeA);
    }

    private static List<OSMNode> getLightNodesAround(int radius, double middleLat, double middleLon)
            throws IOException, ParserConfigurationException, SAXException {
        var lightsQuery = getLightsAroundOverpassQuery(radius, middleLat, middleLon);
        var nodes = getNodesViaOverpass(lightsQuery);
        return getNodes(nodes);
    }


    private static String getLightsAroundOverpassQuery(int radius, double lat, double lon) {
        return "<osm-script>\r\n" +
                "  <query into=\"_\" type=\"node\">\r\n" +
                "    <has-kv k=\"highway\" modv=\"\" v=\"traffic_signals\"/>\r\n" +
                "    <around radius=\"" + radius + "\" lat=\"" + lat + "\" lon=\"" + lon + "\"/>\r\n" +
                "  </query>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" +
                "</osm-script>";
    }

    private static List<OSMNode> sendParentWaysOfLightOverpassQuery(final List<OSMNode> lightsAround)
            throws IOException, ParserConfigurationException, SAXException {
        return parseLightNodeList(
                getNodesViaOverpass(
                        getParentWaysOfLightOverpassQuery(lightsAround)
                )
        );
    }

    private static String getParentWaysOfLightOverpassQuery(final List<OSMNode> lightsAround) {
        StringBuilder builder = new StringBuilder();
        builder.append("<osm-script>");
        for (final OSMNode light : lightsAround) {
            builder.append(getSingleParentWaysOfLightOverpassQuery(light.getId()));
        }
        builder.append("</osm-script>");
        return builder.toString();
    }

    private static String getSingleParentWaysOfLightOverpassQuery(final long osmLightId) {
        return "<id-query type=\"node\" ref=\"" + osmLightId + "\" into=\"crossroad\"/>\r\n" +
                "  <union into=\"_\">\r\n" +
                "    <item from=\"crossroad\" into=\"_\"/>\r\n" +
                "    <recurse from=\"crossroad\" type=\"node-way\"/>\r\n" +
                "  </union>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"full\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>";
    }

    private static void createLightManagers(List<OSMNode> lightsOfTypeA) {
        for (final OSMNode centerCrossroadNode : lightsOfTypeA) {
            if (centerCrossroadNode.determineParentOrientationsTowardsCrossroad()) {
                // TODO: MasterAgent as instance or separate service for adding new agents to container
                MasterAgent.tryAddNewLightManagerAgent(centerCrossroadNode);
            }
        }
    }
}
