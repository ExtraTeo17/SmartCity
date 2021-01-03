package osmproxy.elements;

import java.io.Serializable;

public class OSMStation extends OSMNode implements Serializable {
    private final String stopId;
    private final String stopNumber;
    private final boolean isPlatform;

    public OSMStation(long osmId, double lat, double lng,
                      final String stationRef, boolean isPlatform) {
        super(osmId, lat, lng);
        this.stopNumber = stationRef.substring(stationRef.length() - 2);
        this.stopId = stationRef.substring(0, stationRef.length() - 2);
        this.isPlatform = isPlatform;
    }

    public String getBusStopId() {
        return stopId;
    }

    public String getBusStopNr() {
        return stopNumber;
    }

    public boolean isPlatform() {
        return isPlatform;
    }

    @Override
    public String toString() {
        return "OSMStation{" +
                "osmId=" + id +
                ", lat=" + lat +
                ", lon=" + lon +
                ", stopId='" + stopId + '\'' +
                ", stopNumber='" + stopNumber + '\'' +
                ", isPlatform=" + isPlatform +
                '}';
    }
}
