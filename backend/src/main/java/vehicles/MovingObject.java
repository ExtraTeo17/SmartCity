package vehicles;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.RoutingConstants;
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
    final List<RouteNode> uniformRoute;
    int moveIndex;
    int closestLightIndex;
    DrivingState state;

    MovingObject(int agentId, int speed, List<RouteNode> uniformRoute) {
        this.logger = LoggerFactory.getLogger(this.getClass().getSimpleName() + "Object" + agentId);
        this.agentId = agentId;
        this.speed = speed;
        this.uniformRoute = uniformRoute;
        this.moveIndex = 0;
        this.closestLightIndex = Integer.MAX_VALUE;
        this.state = DrivingState.STARTING;
    }

    public IGeoPosition getPosition() {
        if (moveIndex >= uniformRoute.size()) {
            return uniformRoute.get(uniformRoute.size() - 1);
        }

        return uniformRoute.get(moveIndex);
    }

    /**
     * @return Scaled speed in KM/H
     */
    public int getSpeed() {
        return speed * TimeProvider.TIME_SCALE;
    }

    public void move() {
        ++moveIndex;
        if (moveIndex > uniformRoute.size()) {
            throw new ArrayIndexOutOfBoundsException("MovingObject exceeded its route: " + moveIndex + "/" + uniformRoute.size());
        }
    }

    public abstract String getVehicleType();

    public LightManagerNode switchToNextTrafficLight() {
        for (int i = moveIndex + 1; i < uniformRoute.size(); ++i) {
            var node = uniformRoute.get(i);
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

        return uniformRoute.get(moveIndex) instanceof LightManagerNode;
    }

    public LightManagerNode getCurrentTrafficLightNode() {
        if (closestLightIndex == Integer.MAX_VALUE) {
            return null;
        }
        return (LightManagerNode) (uniformRoute.get(closestLightIndex));
    }

    public boolean isAtDestination() {
        return moveIndex == uniformRoute.size();
    }

    public DrivingState getState() {
        return state;
    }

    public void setState(DrivingState state) {
        this.state = state;
    }

    public int getMillisecondsToNextLight(){
        return ((closestLightIndex - moveIndex) * RoutingConstants.STEP_CONSTANT) / getSpeed();
    }

    public abstract List<RouteNode> getSimpleRoute();

    public abstract long getAdjacentOsmWayId();
}
