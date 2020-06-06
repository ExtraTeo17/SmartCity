package Vehicles;

import java.util.List;

import Routing.RouteNode;
import Routing.StationNode;
import SmartCity.Timetable;

public class Bus extends MovingObjectImpl {
	
	private final Timetable timetable;
	private final List<StationNode> stationNodesOnRoute;
	private final String busLine;
	private final String brigadeNr;

	public Bus(final List<RouteNode> route, final Timetable timetable, final String busLine,
			final String brigadeNr) {
		super(route);
		this.timetable = timetable;
		stationNodesOnRoute = extractStationsFromRoute();
		this.busLine = busLine;
		this.brigadeNr = brigadeNr;
	}

	private List<StationNode> extractStationsFromRoute() {
		try {
			throw new Exception("Not implemented!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public final String getLine() {
		return busLine;
	}
	
	public final List<StationNode> getStationNodesOnRoute() {
		return stationNodesOnRoute;
	}
}
