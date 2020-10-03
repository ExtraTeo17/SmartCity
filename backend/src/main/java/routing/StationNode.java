package routing;

public class StationNode extends RouteNode {
    private final int agentId;
    private final long id;

    public StationNode(double lat, double lon,
                       long id, int agentId) {
        super(lat, lon);
        this.id = id;
        this.agentId = agentId;
    }

    public int getAgentId() {
        return agentId;
    }

    public long getStationNodeId() {
        return id;
    }
}
