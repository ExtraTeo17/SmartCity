package Vehicles;

import Routing.LightManagerNode;
import com.graphhopper.util.PointList;

import Routing.LightManagerNode;
import Routing.RouteNode;

import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

public abstract class Vehicle {
    public abstract String getVehicleType();
    public abstract LightManagerNode findNextTrafficLight();
    public abstract String getPositionString();
    public abstract GeoPosition getPosition();
    public abstract LightManagerNode getCurrentTrafficLightNode();
    public abstract boolean isAtTrafficLights();
    public abstract boolean isAtDestination();
    public abstract void Move();
    public abstract List<RouteNode> getDisplayRoute();
    public abstract long getAdjacentOsmWayId();
    public abstract int getMilisecondsToNextLight();
    public abstract int getSpeed();

    public abstract void setState(DrivingState state);
    public abstract DrivingState getState();
}
