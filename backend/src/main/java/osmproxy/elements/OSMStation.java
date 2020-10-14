package osmproxy.elements;

import java.io.Serializable;

public class OSMStation extends OSMNode implements Serializable {
    private final String stopId;
    private final String stopNumber;

    public OSMStation(long osmId, double lat, double lng, final String stationRef) {
        super(osmId, lat, lng);
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
