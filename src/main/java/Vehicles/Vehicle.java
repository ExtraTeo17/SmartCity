package Vehicles;

import com.graphhopper.util.PointList;

import Routing.LightManagerNode;
import Routing.RouteNode;

import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

public abstract class Vehicle {
    public abstract String getVehicleType();
    public abstract void CalculateRoute();
    public abstract LightManagerNode findNextTrafficLight();
    public abstract String getPositionString();
    public abstract GeoPosition getPosition();
    public abstract String getCurrentTrafficLightID();
    public abstract boolean isAtTrafficLights();
    public abstract boolean isAtDestination();
    public abstract void Move();
    public abstract List<RouteNode> getFullRoute();
    public abstract boolean isAllowedToPass();
    public abstract void setAllowedToPass(boolean value);
    public  abstract long getAdjacentOsmWayId();
}
