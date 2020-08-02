package osmproxy.elements;

public class OSMStation extends OSMNode {

    private String stopWawId;
    private String stopWawNr;

    public OSMStation(final String osmId, final String lat, final String lon, final String stationRef) {
        super(osmId, lat, lon);
        fillStopWawIdNr(stationRef);
    }

    private void fillStopWawIdNr(String stationRef) {
        stopWawNr = stationRef.substring(stationRef.length() - 2);
        stopWawId = stationRef.substring(0, stationRef.length() - 2);
    }

    public String getBusStopId() {
        return stopWawId;
    }

    public String getBusStopNr() {
        return stopWawNr;
    }
}
