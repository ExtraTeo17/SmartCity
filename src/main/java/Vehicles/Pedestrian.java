package Vehicles;

import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import Routing.LightManagerNode;
import Routing.RouteNode;

public class Pedestrian extends MovingObjectImpl {

	public Pedestrian(List<RouteNode> routeToStation, List<RouteNode> routeFromStation,
			final long startingStationId, final String preferredBusLine) {
		super(null);
		try {
			throw new Exception("Not implemented!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
