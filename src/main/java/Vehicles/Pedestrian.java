package Vehicles;

import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import Routing.LightManagerNode;
import Routing.RouteNode;
import Routing.Router;

public class Pedestrian extends MovingObject { // TODO: Remember to create a dedicated super-class for all moving types

	private List<RouteNode> displayRouteBeforeBus, displayRouteAfterBus;
	private List<RouteNode> routeBeforeBus, routeAfterBus;
	private int index = 0;
	private int speed = 40;
	private int closestLightIndex = 0;
	public DrivingState state = DrivingState.STARTING;
	private final long startingStationId;
	private final String preferredBusLine;
	
	public Pedestrian(List<RouteNode> routeToStation, List<RouteNode> routeFromStation,
			final long startingStationId, final String preferredBusLine) {
		displayRouteBeforeBus = routeToStation;
		displayRouteAfterBus = routeFromStation;
		routeBeforeBus = Router.uniformRoute(displayRouteBeforeBus);
		routeAfterBus = Router.uniformRoute(displayRouteAfterBus);
		this.startingStationId = startingStationId;
		this.preferredBusLine = preferredBusLine;
	}

	@Override
	public String getVehicleType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LightManagerNode findNextTrafficLight() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPositionString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeoPosition getPosition() {
		return new GeoPosition(routeBeforeBus.get(index).getLatitude(), routeBeforeBus.get(index).getLongitude());
	}

	@Override
	public LightManagerNode getCurrentTrafficLightNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAtTrafficLights() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAtDestination() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void Move() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<RouteNode> getDisplayRoute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getAdjacentOsmWayId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMilisecondsToNextLight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setState(DrivingState state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DrivingState getState() {
		// TODO Auto-generated method stub
		return null;
	}
}
