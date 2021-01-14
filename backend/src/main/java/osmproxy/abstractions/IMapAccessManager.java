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

public interface IMapAccessManager {
    /**
     * @param xmlDocument parse file into nodes
     * @return a list of openseamap nodes extracted from xml
     */
    List<OSMNode> parseNodes(Document xmlDocument);

    /**
     *
     * @param query - query that needs to be sent
     * @return response from API
     */
    Optional<Document> getNodesDocument(String query);

    /**
     *
     * @param osmWayIds - ids
     * @return lists of light nodes
     */
    List<OSMLight> getOsmLights(List<Long> osmWayIds);


    Optional<RouteInfo> getRouteInfo(List<Long> osmWayIds, boolean notPedestrian);

    /**
     *
     * @param zone in which we are intrested to find lights
     * @return ligth manager nodes
     */
    List<Node> getLightManagersNodes(IZone zone);

    void parseChildNodesOfWays(Document childNodesOfWays, List<OSMNode> lightsOfTypeA);

    Position calculateLatLonBasedOnInternalLights(Node crossroad);

	void initializeWayCache(IZone zone, ICacheWrapper wrapper);
}
