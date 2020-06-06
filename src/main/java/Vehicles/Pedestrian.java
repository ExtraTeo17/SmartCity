package Vehicles;

import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import Routing.LightManagerNode;
import Routing.RouteNode;

public class Pedestrian extends MovingObjectImpl {

	public Pedestrian(List<RouteNode> info) {
		super(info);
	}
}
