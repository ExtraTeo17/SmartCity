package vehicles;


import routing.LightManagerNode;
import routing.RouteNode;
import routing.core.IGeoPosition;
import smartcity.TimeProvider;

import java.util.List;

// TODO: Change name to IVehicle/AbstractVehicle
public abstract class MovingObject {
    final int speed;
    final List<RouteNode> route;
    int moveIndex;
    int closestLightIndex;
    DrivingState state = DrivingState.STARTING;

    MovingObject(int speed, List<RouteNode> route) {
        this.speed = speed;
        this.route = route;
        this.moveIndex = 0;
        this.closestLightIndex = -1;
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

    public LightManagerNode getNextTrafficLight() {
        if (closestLightIndex < route.size() && moveIndex <= closestLightIndex) {
            return (LightManagerNode) route.get(closestLightIndex);
        }

        for (int i = moveIndex; i < route.size(); ++i) {
            var node = route.get(i);
            if (node instanceof LightManagerNode) {
                closestLightIndex = i;
                return (LightManagerNode) node;
            }
        }

        closestLightIndex = Integer.MAX_VALUE;
        return null;
    }

    public boolean isAtTrafficLights() {
        if (isAtDestination()) {
            return false;
        }

        return closestLightIndex == moveIndex;
    }

    public boolean isAtDestination() {
        return moveIndex == route.size();
    }

    public DrivingState getState() {
        return state;
    }

    public void setState(DrivingState state) {
        this.state = state;
    }

    public abstract List<RouteNode> getDisplayRoute();

    public abstract long getAdjacentOsmWayId();

    public abstract int getMillisecondsToNextLight();
}
