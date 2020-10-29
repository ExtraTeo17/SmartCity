package vehicles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.RoutingConstants;
import routing.core.IGeoPosition;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import smartcity.TimeProvider;
import vehicles.enums.DrivingState;

import java.util.ArrayList;
import java.util.List;

// TODO: Change name to IVehicle/AbstractVehicle
public abstract class MovingObject {
    final Logger logger;
    final int agentId;
    final int speed;
    List<RouteNode> simpleRoute;
    List<RouteNode> uniformRoute;
    int moveIndex;
    int closestLightIndex;
    DrivingState state;

    MovingObject(int agentId, int speed, List<RouteNode> uniformRoute, List<RouteNode> simpleRoute) {
        this.logger = LoggerFactory.getLogger(this.getClass().getSimpleName() + "Object" + agentId);
        this.agentId = agentId;
        this.speed = speed;
        this.uniformRoute = uniformRoute;
        this.simpleRoute = simpleRoute;
        this.moveIndex = 0;
        this.closestLightIndex = Integer.MAX_VALUE;
        this.state = DrivingState.STARTING;
    }

    MovingObject(int agentId, int speed, List<RouteNode> uniformRoute) {
        this.logger = LoggerFactory.getLogger(this.getClass().getSimpleName() + "Object" + agentId);
        this.agentId = agentId;
        this.speed = speed;
        this.uniformRoute = uniformRoute;
        this.moveIndex = 0;
        this.closestLightIndex = Integer.MAX_VALUE;
        this.state = DrivingState.STARTING;
    }

    public abstract String getVehicleType();


    public int getMoveIndex() {
    	return moveIndex;
    }

    public int getAgentId() {
        return agentId;
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

    public void setRoutes(final List<RouteNode> simpleRoute, final List<RouteNode> uniformRoute) {
        logger.debug("Set simple route and uniform route");
        this.simpleRoute = simpleRoute;
        this.uniformRoute = uniformRoute;
    }

    public IGeoPosition getStartPosition() {
        return uniformRoute.get(0);
    }

    public IGeoPosition getEndPosition() {
        return uniformRoute.get(uniformRoute.size() - 1);
    }

    public IGeoPosition getPositionOnIndex(int index) {
        if (index >= uniformRoute.size()) {
            return uniformRoute.get(uniformRoute.size() - 1);
        }

        return uniformRoute.get(index);
    }

    //TODO: RETURN TO NORMAL  return uniformRoute.get(moveIndex-1);
    public RouteNode getRouteNodeBeforeLight() {
        if (moveIndex - 1 <= 0) {
            return uniformRoute.get(0);
        }
        if (moveIndex >= uniformRoute.size()) {
            return uniformRoute.get(uniformRoute.size() - 1);
        }
        return uniformRoute.get(moveIndex - 1);
    }

    public IGeoPosition getPosition() {
        return getPositionOnIndex(moveIndex);
    }

    public IGeoPosition getPositionFarOnIndex(int index) {
        return getPositionOnIndex(moveIndex + index);
    }

    public int getFarOnIndex(int index) {
        if (moveIndex + index >= uniformRoute.size()) {
            return uniformRoute.size() - 1;
        }

        return moveIndex + index;
    }

    /**
     * Checks whether an edge exists on the uniformRoute
     *
     * @param ID of the edge checked for existence
     * @return Index of the RouteNode on uniformRoute
     * which contains the edge if edge is found, otherwise null
     */
    public Integer findIndexOfEdgeOnRoute(Long edgeId, int thresholdUntilIndexChange) {
        for (int counter = 0; counter < uniformRoute.size(); ++counter) {
            if (uniformRoute.get(counter).getInternalEdgeId() == edgeId) {
                if (moveIndex + thresholdUntilIndexChange <= counter) {
                    return counter;
                }
            }
        }
        return null;
    }

    public List<RouteNode> getUniformRoute() { return new ArrayList<>(uniformRoute); }

    public int getUniformRouteSize() {return uniformRoute.size();}

    public LightManagerNode switchToNextTrafficLight() {
        logger.debug("Switch to next traffic light");
        for (int i = moveIndex + 1; i < uniformRoute.size(); ++i) {
            var node = uniformRoute.get(i);
            if (node instanceof LightManagerNode) {
                logger.debug("Next traffic light found at uniform route index: " + i);
                closestLightIndex = i;
                return (LightManagerNode) node;
            }
        }

        logger.debug("Next traffic light has not been found");
        closestLightIndex = Integer.MAX_VALUE;
        return null;
    }

    private void displayRouteDebug(List<RouteNode> route) {
        logger.debug("Display route debug of size: " + route.size());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < route.size(); ++i) {
            builder.append("R[" + i + "]: " + route.get(i).getDebugString(route.get(i) instanceof LightManagerNode) + "; ");
        }
        logger.debug(builder.toString());
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
        logger.debug("Get closest light index: " + closestLightIndex);
        return (LightManagerNode) (uniformRoute.get(closestLightIndex));
    }

    // TODO: Sometimes index goes to 0
    public long getAdjacentOsmWayId(int indexFar) {
        int index = moveIndex + indexFar;
        while (index >= 0 && !(uniformRoute.get(index) instanceof LightManagerNode)) {
            --index;
        }

        if (index < 0) {
            return -1;
        }

        return ((LightManagerNode) uniformRoute.get(index)).getAdjacentWayId();
    }

    public long getAdjacentOsmWayId() {
        return getAdjacentOsmWayId(0);
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

    public int getMillisecondsToNextLight() {
        return ((closestLightIndex - moveIndex) * RoutingConstants.STEP_CONSTANT) / getSpeed();
    }

    public int getMillisecondsFromAToB(int startIndex, int finishIndex) {
        return ((finishIndex - startIndex) * RoutingConstants.STEP_CONSTANT) / getSpeed();
    }

    public List<RouteNode> getSimpleRoute() { return simpleRoute; }

    public boolean currentTrafficLightNodeWithinAlternativeRouteThreshold(int thresholdUntilIndexChange) {
        return moveIndex + thresholdUntilIndexChange >= closestLightIndex;
    }
}
