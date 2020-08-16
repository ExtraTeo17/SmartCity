package osmproxy.elements;

public class OSMStation extends OSMNode {
    private final String stopId;
    private final String stopNumber;

    public OSMStation(final String osmId, final String lat, final String lon, final String stationRef) {
        super(osmId, lat, lon);
        stopNumber = stationRef.substring(stationRef.length() - 2);
        stopId = stationRef.substring(0, stationRef.length() - 2);
    }

    public String getBusStopId() {
        return stopId;
    }

    public String getBusStopNr() {
        return stopNumber;
    }
}
