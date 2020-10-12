package vehicles;

import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import routing.RoutingConstants;

import java.util.List;

public class Car extends MovingObject {
    private final List<RouteNode> displayRoute;

    public Car(List<RouteNode> displayRoute, List<RouteNode> uniformRoute) {
        super(50, uniformRoute);
        this.displayRoute = displayRoute;
    }

    Car(Car car) {
        super(car.speed, car.route);
        this.displayRoute = car.displayRoute;
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
    public List<RouteNode> getDisplayRoute() {
        return displayRoute;
    }

    @Override
    public int getMillisecondsToNextLight() {
        return ((closestLightIndex - moveIndex) * RoutingConstants.STEP_CONSTANT) / getSpeed();
    }
}
