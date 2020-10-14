package vehicles;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.core.IGeoPosition;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import smartcity.TimeProvider;

import java.util.List;

// TODO: Change name to IVehicle/AbstractVehicle
public abstract class MovingObject {
    final Logger logger;
    final int agentId;
    final int speed;
    final List<RouteNode> route;
    int moveIndex;
    int closestLightIndex;
    DrivingState state;

    MovingObject(int agentId, int speed, List<RouteNode> route) {
        this.logger = LoggerFactory.getLogger(this.getClass().getSimpleName() + "Object" + agentId);
        this.agentId = agentId;
        this.speed = speed;
        this.route = route;
        this.moveIndex = 0;
        this.closestLightIndex = Integer.MAX_VALUE;
        this.state = DrivingState.STARTING;
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

    public LightManagerNode switchToNextTrafficLight() {
        for (int i = moveIndex + 1; i < route.size(); ++i) {
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

        return route.get(moveIndex) instanceof LightManagerNode;
    }

    public LightManagerNode getCurrentTrafficLightNode() {
        if (closestLightIndex == Integer.MAX_VALUE) {
            return null;
        }
        return (LightManagerNode) (route.get(closestLightIndex));
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
