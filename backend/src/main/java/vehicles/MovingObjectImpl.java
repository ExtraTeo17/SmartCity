package vehicles;

import routing.LightManagerNode;
import routing.RouteNode;
import routing.Router;
import routing.core.Position;

import java.util.List;

// TODO: Maybe rename to Car - more descriptive?
public class MovingObjectImpl extends MovingObject {
    public DrivingState state = DrivingState.STARTING;
    private final List<RouteNode> displayRoute;
    private final List<RouteNode> route;
    private int index = 0;
    private int closestLightIndex = Integer.MAX_VALUE;

    public MovingObjectImpl(List<RouteNode> displayRoute) {
        super(50);
        this.displayRoute = displayRoute;
        // TODO: Inject it via constructor, not create here
        this.route = Router.uniformRoute(displayRoute);
    }

    // TODO: Why car is moving backwards here? Change name of the function to describe behaviour
    @Override
    public long getAdjacentOsmWayId() {
        while (!(route.get(index) instanceof LightManagerNode)) {
            --index;
        }
        return ((LightManagerNode) route.get(index)).getOsmWayId();
    }

    @Override
    public String getVehicleType() {
        return VehicleType.REGULAR_CAR.toString();
    }


    // TODO: Check if behaviour is correct and add test
    @Override
    public LightManagerNode getNextTrafficLight() {
        if (closestLightIndex < route.size() && index <= closestLightIndex) {
            return (LightManagerNode) route.get(closestLightIndex);
        }

        for (int i = index + 1; i < route.size(); ++i) {
            var node = route.get(i);
            if (node instanceof LightManagerNode) {
                closestLightIndex = i;
                return (LightManagerNode) node;
            }
        }

        closestLightIndex = Integer.MAX_VALUE;
        return null;
    }

    @Override
    public Position getPosition() {
        return route.get(index);
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

        return closestLightIndex == index;
    }

    @Override
    public boolean isAtDestination() {
        return index == route.size();
    }

    @Override
    public void move() {
        ++index;
        if (index > route.size()) {
            throw new ArrayIndexOutOfBoundsException("MovingObject exceeded its route: " + index + "/" + route.size());
        }
    }

    @Override
    public List<RouteNode> getDisplayRoute() {
        return displayRoute;
    }

    @Override
    public int getMillisecondsToNextLight() {
        return ((closestLightIndex - index) * Router.STEP_CONSTANT) / getSpeed();
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
