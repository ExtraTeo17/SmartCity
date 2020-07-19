package Vehicles;

import Routing.LightManagerNode;
import Routing.RouteNode;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public abstract class MovingObject {
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

    public abstract int getMillisecondsToNextLight();

    public abstract int getSpeed();

    public abstract DrivingState getState();

    public abstract void setState(DrivingState state);
}
