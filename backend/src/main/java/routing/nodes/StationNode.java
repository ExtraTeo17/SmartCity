package routing.nodes;

import osmproxy.elements.OSMStation;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        StationNode that = (StationNode) o;
        return osmId == that.osmId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), osmId);
    }
}