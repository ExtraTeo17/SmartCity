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
    private Map<String, StationInfo> busLineToAllStationsOnHisRoute = new HashMap<>();
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
    public void addToBusLineStopMap( String busLine,
                                     List<StationNode> mergedStationNodes) {
        if (busLineToAllStationsOnHisRoute.containsKey(busLine)) {
            busLineToAllStationsOnHisRoute.get(busLine).addAll(mergedStationNodes);
        } else {
            busLineToAllStationsOnHisRoute.put(busLine, new StationInfo(List.copyOf(mergedStationNodes)));
        }
    }

    public String findBusLineFromStation(String stationOsmId)
    {
        for (String busLine : busLineToAllStationsOnHisRoute.keySet())
        {
            if(busLineToAllStationsOnHisRoute.get(busLine).stream()
                    .filter(node -> node.getOsmId() == Long.parseLong(stationOsmId)).findFirst().isPresent())
            {
                return busLine;
            }
        }
      return null;
    }

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
