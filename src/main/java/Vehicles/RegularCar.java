package Vehicles;

import GUI.Router;
import Routing.LightManagerNode;
import Routing.RouteNode;

import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

public class RegularCar extends Vehicle {

    private List<RouteNode> displayRoute;

    private List<RouteNode> route;
    private int index = 0;

    private int speed = 40;

    private int closestLightIndex = 0;

    public DrivingState State = DrivingState.STARTING;

    public RegularCar(List<RouteNode> info) {
        displayRoute = info;
        route = Router.uniformRoute(displayRoute);
        for(RouteNode r : route) {
        	if(!r.getClass().getCanonicalName().equals("Routing.RouteNode"))
        	System.out.println(r.getClass().getCanonicalName());
        }
    }

    @Override
    public long getAdjacentOsmWayId() {
        return route.get(index).getOsmWayId();
    }

    @Override
    public String getVehicleType() {
        return "RegularCar";
    }

    @Override
    public LightManagerNode findNextTrafficLight() {
        for (int i = index + 1; i < route.size(); i++) {
            if (route.get(i) instanceof LightManagerNode) {
                closestLightIndex = i;
                System.out.println("FOUND NEW LIGHT MANAGER QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
                return getCurrentTrafficLightNode();
            }
        }
        closestLightIndex = -1;
        return getCurrentTrafficLightNode();
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
        if(closestLightIndex == -1) return null;
        return (LightManagerNode) (route.get(closestLightIndex));
    }

    @Override
    public boolean isAtTrafficLights() {
    if(index==route.size())
    {
    	return false;
    }
        return route.get(index) instanceof LightManagerNode;
    }

    @Override
    public boolean isAtDestination() {
        return index == route.size() ;
    }

    @Override
    public void Move() {
            index++;
    }

    @Override
    public List<RouteNode> getDisplayRoute() {
        return displayRoute;
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
        State = state;
    }

    @Override
    public DrivingState getState() {
        return State;
    }
}