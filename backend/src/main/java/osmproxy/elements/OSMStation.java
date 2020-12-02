package osmproxy.elements;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OSMStation extends OSMNode implements Serializable {
    private final String stopId;
    private final String stopNumber;
    private final boolean isPlatform;
    private final Map<String, String> pedestrianAgentIDPreferredBusLine;

    public OSMStation(long osmId, double lat, double lng,
                      final String stationRef, boolean isPlatform) {
        super(osmId, lat, lng);
        this.stopNumber = stationRef.substring(stationRef.length() - 2);
        this.stopId = stationRef.substring(0, stationRef.length() - 2);
        this.isPlatform = isPlatform;
        this.pedestrianAgentIDPreferredBusLine = new HashMap<>();
        ;
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


    // 1/3: FIX THIS FUNCTION TO TAKE BEST LINE INTO CONSIDERATION, NOT FIRST - done - now in BusManager
    // 2/3: FIX PEDESTRIAN CHOOSING STOPS TO CHOOSE BETWEEN PREV AND NEXT, NOT ONLY NEXT - done
    // 3/3: REFACTOR STATIC BUS CRASH GENERATION PREFERABLY TO  ARGUMENT IN CONSTRUCTOR(looks the same as testCar)

    public void addToAgentMap(String agentName, String desiredBusLine) {
        pedestrianAgentIDPreferredBusLine.put(agentName, desiredBusLine);
    }

    public String getFromAgentMap(String agentName) {
        return pedestrianAgentIDPreferredBusLine.get(agentName);
    }

    public void removeFromAgentMap(String agentName) {
        pedestrianAgentIDPreferredBusLine.remove(agentName);
    }
}
