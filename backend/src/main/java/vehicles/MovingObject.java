package vehicles;


import routing.LightManagerNode;
import routing.RouteNode;
import routing.core.IGeoPosition;
import smartcity.TimeProvider;

import java.util.List;

// TODO: Interface or move some functionality here
// TODO: Change name to IVehicle/AbstractVehicle
public abstract class MovingObject {
    protected final int speed;

    MovingObject(int speed) {this.speed = speed;}

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

    /**
     * @return Scaled speed in KM/H
     */
    public int getSpeed() {
        return speed * TimeProvider.TIME_SCALE;
    }

    public abstract DrivingState getState();

    public abstract void setState(DrivingState state);
}
