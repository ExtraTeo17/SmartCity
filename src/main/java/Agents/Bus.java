package Agents;

import java.util.List;

import Routing.RouteNode;
import Vehicles.RegularCar;
import SmartCity.Timetable;

public class Bus extends RegularCar {
	
	private final Timetable timetable;

	public Bus(final List<RouteNode> route, final Timetable timetable) {
		super(route);
		this.timetable = timetable;
	}
}
