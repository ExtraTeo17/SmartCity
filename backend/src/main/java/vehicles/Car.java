package vehicles;

import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;

import java.util.List;

public class Car extends MovingObject {
    public Car(int agentId, List<RouteNode> displayRoute, List<RouteNode> uniformRoute) {
        super(agentId, 50, uniformRoute, displayRoute);
    }

    Car(Car car) {
        super(car.agentId, car.speed, car.uniformRoute, car.simpleRoute);
    }

    // TODO: Why car is moving backwards here? Change name of the function to describe behaviour
    @Override
    public long getAdjacentOsmWayId() {
        int index = moveIndex;
        while (!(uniformRoute.get(moveIndex) instanceof LightManagerNode)) {
            --moveIndex;
        }
        if (index > moveIndex) {
            logger.warn("I was moving backwards!");
        }
        return ((LightManagerNode) uniformRoute.get(moveIndex)).getAdjacentWayId();
    }


    @Override
    public String getVehicleType() {
        return VehicleType.REGULAR_CAR.toString();
    }
}
