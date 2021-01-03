package routing.nodes;

import osmproxy.elements.OSMStation;

import java.util.Objects;

public class StationNode extends RouteNode {
    private final long osmId;
    private int agentId;
    private OSMStation correspondingPlatformStation;

    public StationNode(double lat, double lon,
                       long osmId,
                       int agentId) {
        super(lat, lon);
        this.osmId = osmId;
        this.agentId = agentId;
    }

    public StationNode(String lat, String lon,
                       String osmId,
                       String agentId) {
        super(Double.parseDouble(lat), Double.parseDouble(lon));
        this.osmId = Long.parseLong(osmId);
        this.agentId = Integer.parseInt(agentId);
    }

    public StationNode(OSMStation station, int agentId) {
        this(station.getLat(), station.getLng(), station.getId(), agentId);
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public long getOsmId() {
        return osmId;
    }

    public OSMStation getCorrespondingPlatformStation() {
        return correspondingPlatformStation;
    }

    public void setCorrespondingPlatformStation(OSMStation correspondingPlatformStation) {
        this.correspondingPlatformStation = correspondingPlatformStation;
    }

    public boolean isPlatform() {
        return correspondingPlatformStation == null;
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
