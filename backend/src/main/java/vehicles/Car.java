package vehicles;

import routing.nodes.RouteNode;
import vehicles.enums.VehicleType;

import java.util.List;

public class Car extends MovingObject {
    public Car(int agentId, List<RouteNode> displayRoute, List<RouteNode> uniformRoute) {
        super(agentId, 50, uniformRoute, displayRoute);
    }

    Car(Car car) {
        super(car.agentId, car.speed, car.uniformRoute, car.simpleRoute);
    }

    @Override
    public String getVehicleType() {
        return VehicleType.REGULAR_CAR.toString();
    }
}
