package Routing;

public class StationNode extends RouteNode {

    private final long stationAgentId;
    private final String osmStationId;

    public StationNode(double lat, double lon, String osmStationId, long stationAgentId) {
        super(lat, lon);
        this.stationAgentId = stationAgentId;
        this.osmStationId = osmStationId;
    }

    public long getStationId() {
        return stationAgentId;
    }

    public String getOsmStationId() {
        return osmStationId;
    }
}
