package routing;

import routing.core.IGeoPosition;

public class StationNode extends RouteNode {
    private final int agentId;
    private final long id;

    public StationNode(double lat, double lon,
                       long id, int agentId) {
        super(lat, lon);
        this.id = id;
        this.agentId = agentId;
    }

    public StationNode(IGeoPosition pos, long id, int agentId){
       this(pos.getLat(), pos.getLng(), id, agentId);
    }

    public int getAgentId() {
        return agentId;
    }

    public long getStationNodeId() {
        return id;
    }
}
