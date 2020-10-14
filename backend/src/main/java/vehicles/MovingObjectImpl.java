package vehicles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.LightManagerNode;
import routing.RouteNode;
import routing.RoutingConstants;

import java.util.List;

// TODO: Maybe rename to Car - more descriptive?
public class MovingObjectImpl extends MovingObject {
    private final Logger logger;
    private final int agentId;
    private final List<RouteNode> displayRoute;

    private transient DrivingState state = DrivingState.STARTING;
    private transient int closestLightIndex = -1;

    public MovingObjectImpl(int agentId, List<RouteNode> displayRoute, List<RouteNode> uniformRoute) {
        super(50, uniformRoute);
        this.agentId = agentId;
        this.displayRoute = displayRoute;
        this.logger = LoggerFactory.getLogger("CarObject" + agentId);
    }

    @SuppressWarnings("CopyConstructorMissesField")
    MovingObjectImpl(MovingObjectImpl movingObject) {
        this(movingObject.agentId, movingObject.displayRoute, movingObject.route);
    }

    // TODO: Why car is moving backwards here? Change name of the function to describe behaviour
    @Override
    public long getAdjacentOsmWayId() {
        int index = moveIndex;
        while (!(route.get(moveIndex) instanceof LightManagerNode)) {
            --moveIndex;
        }
        if (index > moveIndex) {
            logger.warn("I was moving backwards!");
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

        closestLightIndex = -1;
        return null;
    }

    @Override
    public LightManagerNode getCurrentTrafficLightNode() {
        if (closestLightIndex == -1) {
            return null;
        }
        return (LightManagerNode) (route.get(closestLightIndex));
    }

    @Override
    public boolean isAtTrafficLights() {
        if (isAtDestination()) {
            return false;
        }

        return route.get(moveIndex) instanceof LightManagerNode;
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
