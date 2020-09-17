package osmproxy.abstractions;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import osmproxy.elements.OSMLight;
import osmproxy.elements.OSMNode;
import routing.RouteInfo;
import routing.core.IZone;
import routing.core.Position;

import java.util.List;

public interface IMapAccessManager {
    List<OSMNode> getNodes(Document xmlDocument);

    Document getNodesViaOverpass(String query);

    List<OSMLight> sendFullTrafficSignalQuery(List<Long> osmWayIds);

    RouteInfo sendMultipleWayAndItsNodesQuery(List<Long> osmWayIds);

    List<Node> getLightManagersNodes(IZone zone);

    void parseChildNodesOfWays(Document childNodesOfWays, List<OSMNode> lightsOfTypeA);

    Position calculateLatLonBasedOnInternalLights(Node crossroad);
}
