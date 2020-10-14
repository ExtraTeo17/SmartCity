package vehicles;

import routing.RoutingConstants;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;

import java.util.List;

public class Car extends MovingObject {
    private final List<RouteNode> displayRoute;

    public Car(int agentId, List<RouteNode> displayRoute, List<RouteNode> uniformRoute) {
        super(agentId,50, uniformRoute);
        this.displayRoute = displayRoute;
    }

    Car(Car car) {
        super(car.agentId, car.speed, car.route);
        this.displayRoute = car.displayRoute;
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
    public List<RouteNode> getDisplayRoute() {
        return displayRoute;
    }

    @Override
    public int getMillisecondsToNextLight() {
        return ((closestLightIndex - moveIndex) * RoutingConstants.STEP_CONSTANT) / getSpeed();
    }
}
