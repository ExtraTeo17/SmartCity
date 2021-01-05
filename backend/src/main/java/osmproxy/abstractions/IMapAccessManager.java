package osmproxy.abstractions;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import osmproxy.elements.OSMLight;
import osmproxy.elements.OSMNode;
import routing.RouteInfo;
import routing.core.IZone;
import routing.core.Position;

import java.util.List;
import java.util.Optional;
//TODO:dokumentacja

public interface IMapAccessManager {
    List<OSMNode> parseNodes(Document xmlDocument);

    Optional<Document> getNodesDocument(String query);

    List<OSMLight> getOsmLights(List<Long> osmWayIds);

    Optional<RouteInfo> getRouteInfo(List<Long> osmWayIds, boolean notPedestrian);

    List<Node> getLightManagersNodes(IZone zone);

    void parseChildNodesOfWays(Document childNodesOfWays, List<OSMNode> lightsOfTypeA);

    Position calculateLatLonBasedOnInternalLights(Node crossroad);
}
