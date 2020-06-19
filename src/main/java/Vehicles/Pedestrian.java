package Vehicles;

import java.util.List;
import java.util.ArrayList;

import Routing.StationNode;
import org.jxmapviewer.viewer.GeoPosition;

import Routing.LightManagerNode;
import Routing.RouteNode;
import Routing.Router;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

@SuppressWarnings("restriction")
public class Pedestrian extends MovingObject { // TODO: Remember to create a dedicated super-class for all moving types

    private List<RouteNode> displayRouteBeforeBus, displayRouteAfterBus;
    private List<RouteNode> routeBeforeBus, routeAfterBus;
    private List<RouteNode> route = new ArrayList<>();
    private int index = 0;
    private int speed = 40;
    private int closestLightIndex = 0;
    private int stationIndex = 0;
    public DrivingState state = DrivingState.STARTING;
    private final String preferredBusLine;

    public Pedestrian(List<RouteNode> routeToStation, List<RouteNode> routeFromStation,
                      final long startingStationId, final String preferredBusLine) {
        displayRouteBeforeBus = routeToStation;
        displayRouteAfterBus = routeFromStation;
        routeBeforeBus = Router.uniformRoute(displayRouteBeforeBus);
        routeAfterBus = Router.uniformRoute(displayRouteAfterBus);
        route.addAll(routeBeforeBus);
        route.addAll(routeAfterBus);
        stationIndex = routeBeforeBus.size() - 1;
        this.preferredBusLine = preferredBusLine;
    }

    public StationNode getStartingStation() {
        return (StationNode) route.get(stationIndex);
    }

    public StationNode getTargetStation() {
        return (StationNode) route.get(stationIndex + 1);
    }

    public String getPreferredBusLine() {
        return preferredBusLine;
    }

    @Override
    public long getAdjacentOsmWayId() {
        return ((LightManagerNode) route.get(index)).getOsmWayId();
    }

    @Override
    public String getVehicleType() {
        return "Pedestrian";
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
        if (closestLightIndex == -1)
            return null;
        return (LightManagerNode) (route.get(closestLightIndex));
    }

    @Override
    public boolean isAtTrafficLights() {
        if (index == route.size())
            return false;
        return route.get(index) instanceof LightManagerNode;
    }

    public boolean isAtStation() {
        if (index == route.size())
            return false;
        return route.get(index) instanceof StationNode;
    }

    @Override
    public boolean isAtDestination() {
        return index == route.size();
    }

    @Override
    public void Move() {
        index++;
    }

    @Override
    public List<RouteNode> getDisplayRoute() {
        throw new NotImplementedException();
    }

    @Override
    public int getMilisecondsToNextLight() {
        return ((closestLightIndex - index) * 3600) / getSpeed();
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public void setState(DrivingState state) {
        state = state;
    }

    @Override
    public DrivingState getState() {
        return state;
    }

    public List<RouteNode> getDisplayRouteBeforeBus() {
        return displayRouteBeforeBus;
    }

    public List<RouteNode> getDisplayRouteAfterBus() {
        return displayRouteAfterBus;
    }
}
