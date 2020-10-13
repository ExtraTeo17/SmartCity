package vehicles;

import routing.LightManagerNode;
import routing.RouteNode;
import routing.RoutingConstants;

import java.util.List;

// TODO: Maybe rename to Car - more descriptive?
public class MovingObjectImpl extends MovingObject {
    private final List<RouteNode> displayRoute;

    private transient DrivingState state = DrivingState.STARTING;
    private transient int closestLightIndex = Integer.MAX_VALUE;

    public MovingObjectImpl(List<RouteNode> displayRoute, List<RouteNode> uniformRoute) {
        super(50, uniformRoute);
        this.displayRoute = displayRoute;
    }

    MovingObjectImpl(MovingObjectImpl movingObject) {
        super(movingObject.speed, movingObject.route);
        this.displayRoute = movingObject.displayRoute;
    }

    // TODO: Why car is moving backwards here? Change name of the function to describe behaviour
    @Override
    public long getAdjacentOsmWayId() {
        while (!(route.get(moveIndex) instanceof LightManagerNode)) {
            --moveIndex;
        }
        return ((LightManagerNode) route.get(moveIndex)).getAdjacentWayId();
    }

    @Override
    public String getVehicleType() {
        return VehicleType.REGULAR_CAR.toString();
    }

    @Override
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

    // TODO: Delete this function - replaced with getNextTrafficLight
    @Deprecated
    @Override
    public LightManagerNode getCurrentTrafficLightNode() {
        if (closestLightIndex == -1) {
            return null;
        }
        return (LightManagerNode) (route.get(closestLightIndex));
    }

    // TODO: Behaviour was changed, confirm that is correct and add test.
    @Override
    public boolean isAtTrafficLights() {
        if (isAtDestination()) {
            return false;
        }

        return closestLightIndex == moveIndex;
    }

    @Override
    public boolean isAtDestination() {
        return moveIndex == route.size();
    }

    @Override
    public List<RouteNode> getDisplayRoute() {
        return displayRoute;
    }

    @Override
    public int getMillisecondsToNextLight() {
        return ((closestLightIndex - moveIndex) * RoutingConstants.STEP_CONSTANT) / getSpeed();
    }

    @Override
    public DrivingState getState() {
        return state;
    }

    @Override
    public void setState(DrivingState state) {
        this.state = state;
    }
}
