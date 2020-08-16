package osmproxy;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import routing.IZone;
import smartcity.MasterAgent;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


// TODO: Interface
@Beta
public class LightAccessManager extends MapAccessManager {
    private final IZone zone;

    @Inject
    public LightAccessManager(IZone zone) {
        this.zone = zone;
    }

    public void constructLightManagers()
            throws IOException, ParserConfigurationException, SAXException {
        List<OSMNode> lightsAround = getLightNodesAround();
        List<OSMNode> lightNodeList = sendParentWaysOfLightOverpassQuery(lightsAround);
        List<OSMNode> lightsOfTypeA = lightNodeList.stream().filter(OSMNode::isTypeA).collect(Collectors.toList());

        createLightManagers(lightsOfTypeA);
    }

    private List<OSMNode> getLightNodesAround()
            throws IOException, ParserConfigurationException, SAXException {
        var lightsQuery = OsmQueryManager.getLightsAroundOverpassQuery(zone.getCenter(), zone.getRadius());
        var nodes = MapAccessManager.getNodesViaOverpass(lightsQuery);
        return MapAccessManager.getNodes(nodes);
    }

    private List<OSMNode> parseLightNodeList(Document nodesViaOverpass) {
        List<OSMNode> lightNodeList = new ArrayList<>();
        Node osmRoot = nodesViaOverpass.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            parseLightNode(lightNodeList, osmXMLNodes.item(i));
        }
        return lightNodeList;
    }

    private List<OSMNode> sendParentWaysOfLightOverpassQuery(final List<OSMNode> lightsAround)
            throws IOException, ParserConfigurationException, SAXException {
        return parseLightNodeList(
                MapAccessManager.getNodesViaOverpass(
                        getParentWaysOfLightOverpassQuery(lightsAround)
                )
        );
    }

    private void parseLightNode(List<OSMNode> lightNodeList, Node item) {
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

    private String getParentWaysOfLightOverpassQuery(final List<OSMNode> lightsAround) {
        StringBuilder builder = new StringBuilder();
        for (final OSMNode light : lightsAround) {
            builder.append(OsmQueryManager.getSingleParentWaysOfLightOverpassQuery(light.getId()));
        }
        return OsmQueryManager.getQueryWithPayload(builder.toString());
    }

    private void createLightManagers(List<OSMNode> lightsOfTypeA) {
        for (final OSMNode centerCrossroadNode : lightsOfTypeA) {
            if (centerCrossroadNode.determineParentOrientationsTowardsCrossroad()) {
                // TODO: MasterAgent as instance or separate service for adding new agents to container
                MasterAgent.tryCreateLightManager(centerCrossroadNode);
            }
        }

    }
}
