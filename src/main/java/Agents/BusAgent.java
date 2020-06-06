package Agents;

import java.util.List;
import java.util.Random;

import Routing.RouteNode;
import Routing.StationNode;
import SmartCity.Timetable;
import Vehicles.Bus;

import org.javatuples.Pair;
import org.jxmapviewer.viewer.GeoPosition;

public class BusAgent extends VehicleAgent {

    private final long agentId;
    private final Bus bus;
    
	public BusAgent(final List<RouteNode> route, final Timetable timetable,
			final String busLine, final String brigadeNr, final int busId) {
		agentId = busId;
		bus = new Bus(route, timetable, busLine, brigadeNr);
	}

	public Bus getBus() { return bus; }

	public String getId() {
		return Long.toString(agentId);
	}

	public final Pair<StationNode, StationNode> getTwoSubsequentStations(final Random random) {
		List<StationNode> stationsOnRoute = bus.getStationNodesOnRoute();
		final int halfIndex = stationsOnRoute.size() / 2;
		return Pair.with(stationsOnRoute.get(random.nextInt(halfIndex)),
				stationsOnRoute.get(halfIndex + random.nextInt(halfIndex) - 1));
	}

	public final String getLine() {
		return bus.getLine();
	}
}
