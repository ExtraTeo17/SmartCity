package routing;

import osmproxy.elements.OSMStation;

public class StationNode extends RouteNode {
    private final int agentId;
    private final long osmId;

    public StationNode(double lat, double lon,
                       long osmId, int agentId) {
        super(lat, lon);
        this.osmId = osmId;
        this.agentId = agentId;
    }

    public StationNode(OSMStation station, int agentId) {
        this(station.getLat(), station.getLng(), station.getId(), agentId);
    }

    public int getAgentId() {
        return agentId;
    }

    public long getOsmId() {
        return osmId;
    }
}
