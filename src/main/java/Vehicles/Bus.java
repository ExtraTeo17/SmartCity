package Vehicles;

import java.util.List;

import Routing.RouteNode;
import SmartCity.Timetable;

public class Bus extends MovingObjectImpl {
	
	private final Timetable timetable;

	public Bus(final List<RouteNode> route, final Timetable timetable) {
		super(route);
		this.timetable = timetable;
	}
}
