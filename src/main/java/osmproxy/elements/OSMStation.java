package osmproxy.elements;

import java.util.Map;

public class OSMStation extends OSMNode {

    private String stopWawId;
    private String stopWawNr;

    public OSMStation(String id, String latitude, String longitude, String version2, Map<String, String> tags2) {
        super(id, latitude, longitude);
    }

    public OSMStation(final String osmId, final String lat, final String lon, final String stationRef) {
        super(osmId, lat, lon);
        fillStopWawIdNr(stationRef);
    }

    private final void fillStopWawIdNr(String stationRef) {
        stopWawNr = stationRef.substring(stationRef.length() - 2, stationRef.length());
        stopWawId = stationRef.substring(0, stationRef.length() - 2);
    }

    public String getBusStopId() {
        return stopWawId;
    }

    public String getBusStopNr() {
        return stopWawNr;
    }
}
