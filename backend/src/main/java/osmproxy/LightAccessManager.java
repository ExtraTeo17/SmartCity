package osmproxy;

import com.google.common.annotations.Beta;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import smartcity.MasterAgent;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
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
        var lightsQuery = OsmQueryManager.getLightsAroundOverpassQuery(radius, middleLat, middleLon);
        var nodes = getNodesViaOverpass(lightsQuery);
        return getNodes(nodes);
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

    private static List<OSMNode> sendParentWaysOfLightOverpassQuery(final List<OSMNode> lightsAround)
            throws IOException, ParserConfigurationException, SAXException {
        return parseLightNodeList(
                getNodesViaOverpass(
                        getParentWaysOfLightOverpassQuery(lightsAround)
                )
        );
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

    private static String getParentWaysOfLightOverpassQuery(final List<OSMNode> lightsAround) {
        StringBuilder builder = new StringBuilder();
        for (final OSMNode light : lightsAround) {
            builder.append(OsmQueryManager.getSingleParentWaysOfLightOverpassQuery(light.getId()));
        }
        return OsmQueryManager.getQueryWithPayload(builder.toString());
    }

    private static void createLightManagers(List<OSMNode> lightsOfTypeA) {
        for (final OSMNode centerCrossroadNode : lightsOfTypeA) {
            if (centerCrossroadNode.determineParentOrientationsTowardsCrossroad()) {
                // TODO: MasterAgent as instance or separate service for adding new agents to container
                MasterAgent.tryCreateLightManager(centerCrossroadNode);
            }
        }

    }
}
