package SmartCity.Buses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import OSMProxy.Elements.OSMWay;
import OSMProxy.MapAccessManager;
import OSMProxy.Elements.OSMStation;
import Routing.RouteNode;
import Routing.Router;
import SmartCity.SmartCityAgent;
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

	public List<OSMStation> getStations() {
		List<OSMStation> stations = new ArrayList<>();
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

	public void filterStationsByCircle(double middleLat, double middleLon, int radius) {
		List<Long> filteredStationOsmIds = new ArrayList<>();
		for (Long osmStationId : stationsOnRouteOsmIds) {
			OSMStation station = SmartCityAgent.osmIdToStationOSMNode.get(osmStationId);
			if (MapAccessManager.belongsToCircle(station.getLat(), station.getLon(),
					new GeoPosition(middleLat, middleLon), radius)) {
				filteredStationOsmIds.add(osmStationId);
			}
		}
		stationsOnRouteOsmIds = filteredStationOsmIds;
	}
}
