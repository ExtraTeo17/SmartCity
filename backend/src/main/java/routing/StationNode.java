package routing;

public class StationNode extends RouteNode {
    private final int stationAgentId;
    private final String osmStationId;

    public StationNode(double lat, double lon, String osmStationId, int stationAgentId) {
        super(lat, lon);
        this.stationAgentId = stationAgentId;
        this.osmStationId = osmStationId;
    }

    public int getStationId() {
        return stationAgentId;
    }

    public String getOsmStationId() {
        return osmStationId;
    }
}
