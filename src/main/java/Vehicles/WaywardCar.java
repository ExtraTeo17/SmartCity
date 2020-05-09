package Vehicles;

import com.graphhopper.util.PointList;

import Routing.LightManagerNode;
import Routing.RouteNode;

import java.util.List;

import org.javatuples.Pair;
import org.jxmapviewer.viewer.GeoPosition;

public class WaywardCar extends Vehicle {

    private List<RouteNode> route;
    private int index = 0;
    private boolean passCheck = false;

    public WaywardCar(List<RouteNode> info) {
        route = info;
    }

    @Override
    public String getVehicleType() {
        return "WaywardCar";
    }

    @Deprecated
    @Override
    public void CalculateRoute() { }

    @Override
    public String findNextTrafficLight() {
        //return index == 0 || route.get(index - 1) instanceof LightManagerNode;
    	return "";
    }

    @Override
    public String getPositionString() {
        return "Lat: " + route.get(index).lat + " Lon: " + route.get(index).lon;
    }

    @Override
    public GeoPosition getPosition() {
        return new GeoPosition(route.get(index).lat, route.get(index).lon);
    }

    @Override
    public String getCurrentTrafficLightID() {
        return ((LightManagerNode)(route.get(index))).lightManagerId;
    }

    @Override
    public boolean isAtTrafficLights() {
        return route.get(index) instanceof LightManagerNode;
    }

    @Override
    public boolean isAtDestination() {
        return index == route.size() - 1;
    }

    @Override
    public void Move() {
        if (!isAtTrafficLights() || (isAtTrafficLights() && isAllowedToPass()))
        {
            index++;
            setAllowedToPass(false);
        }
    }

    @Override
    public List<RouteNode> getFullRoute() {
        return route;
    }

    @Override
    public boolean isAllowedToPass() {
        return passCheck;
    }

    @Override
    public void setAllowedToPass(boolean value) {
        passCheck = value;
    }
}

/*
13 elementowa (3 light managery):

x x M x x x x x M x x M x x

*/
