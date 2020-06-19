package SmartCity.Stations;

import OSMProxy.Elements.OSMStation;
import Routing.StationNode;
import SmartCity.SmartCityAgent;

public class StationStrategy {

	//private final OSMStation stationOSMNode;
	public StationStrategy(OSMStation stationOSMNode, long agentId) {
		SmartCityAgent.osmStationIdToStationNode.put(stationOSMNode.getId(), new StationNode(stationOSMNode.getLat(),
				stationOSMNode.getLon(), Long.toString(stationOSMNode.getId()), agentId));
	}
}
