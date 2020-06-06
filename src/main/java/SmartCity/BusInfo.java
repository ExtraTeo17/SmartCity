package SmartCity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import GUI.Router;
import Routing.RouteNode;
import jade.wrapper.AgentContainer;
// Bus Lines
public class BusInfo {

	private String busLine;
	private List<OSMWay> route = new ArrayList<>();
	private List<BrigadeInfo> brigadeList = new ArrayList<>(); 
	private List<Long> stationsOnRouteOsmIds = new ArrayList<>();

	public void setBusLine(String nodeValue) {
		busLine = nodeValue;
	}
	
	public String getBusLine() {
		return busLine;
	}

	public void addStation(String nodeValue) {
		stationsOnRouteOsmIds.add(Long.parseLong(nodeValue));
	}

	public List<StationOSMNode> getStations() {
		List<StationOSMNode> stations = new ArrayList<>();
		for (long osmId : stationsOnRouteOsmIds) {
			stations.add(SmartCityAgent.osmIdToStationOSMNode.get(osmId));
		}
		return stations;
	}

	public void setRoute(List<OSMWay> parseOsmWay) {
		route = parseOsmWay;
	}

	public void setBrigadeList(Collection<BrigadeInfo> values) {
		brigadeList = new ArrayList<>(values);
	}

	public void prepareAgents(AgentContainer container) {
		List<RouteNode> routeWithNodes = Router.generateRouteInfoForBuses(route, stationsOnRouteOsmIds);
		for (BrigadeInfo brigade : brigadeList) {
			brigade.prepareAgents(container, routeWithNodes, busLine);
		}
	}

	public Long getLastStation() {
		if (stationsOnRouteOsmIds.size() == 0)
			return (long) 0;
		return stationsOnRouteOsmIds.get(stationsOnRouteOsmIds.size() - 1);
	}

	
}
