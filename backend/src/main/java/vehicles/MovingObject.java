package vehicles;


import routing.IGeoPosition;
import routing.LightManagerNode;
import routing.RouteNode;

import java.util.List;

// TODO: Interface or move some functionality here
// TODO: Change name to IVehicle/AbstractVehicle
public abstract class MovingObject {
    public abstract String getVehicleType();

    public abstract LightManagerNode getNextTrafficLight();

    public abstract IGeoPosition getPosition();

    public abstract LightManagerNode getCurrentTrafficLightNode();

    public abstract boolean isAtTrafficLights();

    public abstract boolean isAtDestination();

    public abstract void move();

    public abstract List<RouteNode> getDisplayRoute();

    public abstract long getAdjacentOsmWayId();

    public abstract int getMillisecondsToNextLight();

    public abstract int getSpeed();

    public abstract DrivingState getState();

    public abstract void setState(DrivingState state);
}
