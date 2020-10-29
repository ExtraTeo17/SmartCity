package routing.nodes;

import osmproxy.elements.OSMStation;
import smartcity.recreationalplaces.OSMCafe;

public class CafeNode extends RouteNode {
    private long osmId;
    private final int agentId;


    public CafeNode(double lat, double lng, long osmId, int agentId) {
        super(lat, lng);
        this.osmId = osmId;
        this.agentId=agentId;
    }
    public CafeNode(OSMCafe cafe, int agentId) {
        this(cafe.getLat(), cafe.getLng(), cafe.getId(), agentId);
    }
}
