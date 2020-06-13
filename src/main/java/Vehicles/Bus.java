package Vehicles;

import java.util.ArrayList;
import java.util.List;

import Routing.LightManagerNode;
import Routing.RouteNode;
import Routing.Router;
import Routing.StationNode;
import SmartCity.Buses.Timetable;

import org.jxmapviewer.viewer.GeoPosition;

public class Bus extends MovingObject {

	private final Timetable timetable;
	private final List<StationNode> stationNodesOnRoute;
	private final String busLine;
	private final String brigadeNr;
	private List<RouteNode> displayRoute;
	private List<RouteNode> route;
	private int index = 0;
	private int speed = 30;
	private int closestLightIndex = -1;
	private int closestStationIndex = -1;

	public Bus(final List<RouteNode> route, final Timetable timetable, final String busLine,
			   final String brigadeNr) {
		displayRoute = route;
		this.route = Router.uniformRoute(displayRoute);
		this.timetable = timetable;
		stationNodesOnRoute = extractStationsFromRoute();
		this.busLine = busLine;
		this.brigadeNr = brigadeNr;
	}

	private List<StationNode> extractStationsFromRoute() {
		List<StationNode> stationsOnRoute = new ArrayList<>();
		for (final RouteNode node : route) {
			if (node instanceof StationNode)
				stationsOnRoute.add((StationNode)node);
		}
		return stationsOnRoute;
	}

	public final String getLine() {
		return busLine;
	}

	public final List<StationNode> getStationNodesOnRoute() {
		return stationNodesOnRoute;

	}

	public DrivingState State = DrivingState.STARTING;

	@Override
	public long getAdjacentOsmWayId() {
		return ((LightManagerNode)route.get(index)).getOsmWayId();
	}

	@Override
	public String getVehicleType() {
		return "Bus";
	}

	@Override
	public LightManagerNode findNextTrafficLight() {
		for (int i = index + 1; i < route.size(); i++) {
			if (route.get(i) instanceof LightManagerNode) {
				closestLightIndex = i;
				return getCurrentTrafficLightNode();
			}
		}
		closestLightIndex = -1;
		return getCurrentTrafficLightNode();
	}

	public StationNode findNextStation() {
		for (int i = index + 1; i < route.size(); i++) {
			if (route.get(i) instanceof StationNode) {
				closestStationIndex = i;
				return (StationNode) route.get(i);
			}
		}
		closestStationIndex = -1;
		return null;
	}

	public RouteNode findNextStop() {
		for (int i = index + 1; i < route.size(); i++) {
			if (route.get(i) instanceof StationNode) {
				return (StationNode) route.get(i);
			}
			if (route.get(i) instanceof LightManagerNode) {
				return (LightManagerNode) route.get(i);
			}
		}
		return null;
	}

	@Override
	public String getPositionString() {
		return "Lat: " + route.get(index).getLatitude() + " Lon: " + route.get(index).getLongitude();
	}

	@Override
	public GeoPosition getPosition() {
		return new GeoPosition(route.get(index).getLatitude(), route.get(index).getLongitude());
	}

	@Override
	public LightManagerNode getCurrentTrafficLightNode() {
		if (closestLightIndex == -1) return null;
		return (LightManagerNode) (route.get(closestLightIndex));
	}

	@Override
	public boolean isAtTrafficLights() {
		if (index == route.size()) {
			return false;
		}
		return route.get(index) instanceof LightManagerNode;
	}

	public boolean isAtStation() {
		if (index == route.size()) {
			return true;
		}
		return route.get(index) instanceof StationNode;
	}

	public StationNode getCurrentStationNode() {
		if (closestStationIndex == -1) return null;
		return (StationNode) (route.get(closestStationIndex));
	}

	@Override
	public boolean isAtDestination() {
		return index == route.size();
	}

	@Override
	public void Move() {
		if (isAtDestination()) index = 0;
		else index++;
	}

	@Override
	public List<RouteNode> getDisplayRoute() {
		return displayRoute;
	}

	@Override
	public int getMilisecondsToNextLight() {
		return ((closestLightIndex - index) * 3600) / getSpeed();
	}

	public int getMilisecondsToNextStation() {
		return ((closestStationIndex - index) * 3600) / getSpeed();
	}

	@Override
	public int getSpeed() {
		return speed;
	}

	@Override
	public void setState(DrivingState state) {
		State = state;
	}

	@Override
	public DrivingState getState() {
		return State;
	}
}
