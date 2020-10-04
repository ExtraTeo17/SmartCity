package vehicles;


import routing.LightManagerNode;
import routing.RouteNode;
import routing.core.IGeoPosition;
import smartcity.TimeProvider;

import java.util.List;

// TODO: Interface or move some functionality here
// TODO: Change name to IVehicle/AbstractVehicle
public abstract class MovingObject {
    final int speed;
    final List<RouteNode> route;
    int moveIndex;

    MovingObject(int speed, List<RouteNode> route) {
        this.speed = speed;
        this.route = route;
        this.moveIndex = 0;
    }

    public IGeoPosition getPosition() {
        if (moveIndex >= route.size()) {
            return route.get(route.size() - 1);
        }

        return route.get(moveIndex);
    }

    /**
     * @return Scaled speed in KM/H
     */
    public int getSpeed() {
        return speed * TimeProvider.TIME_SCALE;
    }

    public void move() {
        ++moveIndex;
        if (moveIndex > route.size()) {
            throw new ArrayIndexOutOfBoundsException("MovingObject exceeded its route: " + moveIndex + "/" + route.size());
        }
    }

    public abstract String getVehicleType();

    public abstract LightManagerNode getNextTrafficLight();

    public abstract LightManagerNode getCurrentTrafficLightNode();

    public abstract boolean isAtTrafficLights();

    public abstract boolean isAtDestination();

    public abstract List<RouteNode> getDisplayRoute();

    public abstract long getAdjacentOsmWayId();

    public abstract int getMillisecondsToNextLight();

    public abstract DrivingState getState();

    public abstract void setState(DrivingState state);
}
