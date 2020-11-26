package osmproxy.elements;

import routing.nodes.StationNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OSMStation extends OSMNode implements Serializable {
    private final String stopId;
    private final String stopNumber;

    private Map<String, String> pedestrianAgentIDPreferredBusLine = new HashMap<>();

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



    // 1/3: FIX THIS FUNCTION TO TAKE BEST LINE INTO CONSIDERATION, NOT FIRST - done - now in BusManager
    // 2/3: FIX PEDESTRIAN CHOOSING STOPS TO CHOOSE BETWEEN PREV AND NEXT, NOT ONLY NEXT - done
    // 3/3: REFACTOR STATIC BUS CRASH GENERATION PREFERABLY TO TEST BUS

    public void addToAgentMap(String agentName, String desiredBusLine) {
        pedestrianAgentIDPreferredBusLine.put(agentName,desiredBusLine);
    }
    public String getFromAgentMap(String agentName) {
       return pedestrianAgentIDPreferredBusLine.get(agentName);
    }
    public void removeFromAgentMap(String agentName) {
        pedestrianAgentIDPreferredBusLine.remove(agentName);
    }
}
