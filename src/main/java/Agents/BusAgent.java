package Agents;

import java.util.List;

import Routing.RouteNode;
import SmartCity.Timetable;
import Vehicles.Bus;

import org.jxmapviewer.viewer.GeoPosition;

public class BusAgent extends VehicleAgent {

    private final long agentId;
    private final Bus bus;
    
	public BusAgent(final List<RouteNode> route, final Timetable timetable, final int busId) {
		agentId = busId;
		bus = new Bus(route, timetable);
	}

	public Bus getBus() { return bus; }

	public String getId() {
		return Long.toString(agentId);
	}
}
