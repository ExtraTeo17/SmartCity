package osmproxy;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import osmproxy.abstractions.ILightAccessManager;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import routing.core.IZone;
import utilities.ConditionalExecutor;
import utilities.FileWriterWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


// TODO: Interface
@Beta
public class LightAccessManager implements ILightAccessManager {
    private static final Logger logger = LoggerFactory.getLogger(LightAccessManager.class);

    private final IZone zone;
    private final IMapAccessManager mapAccessManager;

    @Inject
    public LightAccessManager(IMapAccessManager mapAccessManager, IZone zone) {
        this.mapAccessManager = mapAccessManager;
        this.zone = zone;
    }

    @Override
    public List<OSMNode> getLightsOfTypeA() {
        List<OSMNode> lightsAround = getLightNodesInZone();
        List<OSMNode> lightNodeList = getLightNodes(lightsAround);

        return lightNodeList.stream().filter(OSMNode::isTypeA).collect(Collectors.toList());
    }

    private List<OSMNode> getLightNodesInZone() {
        var lightsQuery = OsmQueryManager.getLightsAroundQuery(zone.getCenter(), zone.getRadius());
        var nodes = mapAccessManager.getNodesDocument(lightsQuery);
        if (nodes.isEmpty()) {
            logger.warn("Failed to get lightNodes due to empty document");
            return new ArrayList<>();
        }

        return mapAccessManager.parseNodes(nodes.get());
    }

    private List<OSMNode> getLightNodes(final List<OSMNode> lightsAround) {
        var query = getParentWaysOfLightsQuery(lightsAround);
        var documentOpt = mapAccessManager.getNodesDocument(query);
        if (documentOpt.isEmpty()) {
            logger.warn("Failed to getLightNodes due to emptyDocument");
            return new ArrayList<>();
        }

        var document = documentOpt.get();
        ConditionalExecutor.debug(() -> {
            String path = "target/lights_" +
                    lightsAround.get(0).getId() + "_" +
                    lightsAround.get(lightsAround.size() - 1).getId() +
                    ".xml";
            logger.info("Writing lightNodes document to file: " + path);
            FileWriterWrapper.write(document, path);
        });

        return parseLightNodesDocument(document);
    }

    private List<OSMNode> parseLightNodesDocument(Document nodesViaOverpass) {
        List<OSMNode> lightNodes = new ArrayList<>();
        Node osmRoot = nodesViaOverpass.getFirstChild();
        NodeList osmNodes = osmRoot.getChildNodes();
        for (int i = 1; i < osmNodes.getLength(); ++i) {
            var item = osmNodes.item(i);
            if (item.getNodeName().equals("node")) {
                OSMNode nodeWithParents = new OSMNode(item.getAttributes());
                lightNodes.add(nodeWithParents);
            }
            else if (item.getNodeName().equals("way")) {
                OSMWay osmWay = new OSMWay(item);
                OSMNode listLastLight = lightNodes.get(lightNodes.size() - 1);
                if (osmWay.isOneWayAndLightContiguous(listLastLight.getId())) {
                    listLastLight.addParentWay(osmWay);
                }
            }
        }

        return lightNodes;
    }

    private String getParentWaysOfLightsQuery(final List<OSMNode> lightsAround) {
        StringBuilder builder = new StringBuilder();
        for (final OSMNode light : lightsAround) {
            builder.append(OsmQueryManager.getSingleParentWaysOfLightQuery(light.getId()));
        }

        return OsmQueryManager.getQueryWithPayload(builder.toString());
    }
}
