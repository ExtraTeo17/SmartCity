package SmartCity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BusInfo {

	private int busLine;
	private List<OSMWay> route = new ArrayList<>();
	private List<Timetable> timetable = new ArrayList<>();
	private List<Long> stationsOnRouteOsmIds = new ArrayList<>();

	public void setBusLine(String nodeValue) {
		busLine = Integer.parseInt(nodeValue);
	}
	
	public int getBusLine() {
		return busLine;
	}

	public void addStation(String nodeValue) {
		stationsOnRouteOsmIds.add(Long.parseLong(nodeValue));
	}

	public List<Station> getStations() {
		List<Station> stations = new ArrayList<>();
		for (long osmId : stationsOnRouteOsmIds) {
			stations.add(SmartCityAgent.stations.get(osmId));
		}
		return stations;
	}

	public void setList(List<OSMWay> parseOsmWay) {
		route = parseOsmWay;
	}
}
