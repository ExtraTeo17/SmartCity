package vehicles;

import routing.nodes.RouteNode;
import smartcity.ITimeProvider;
import vehicles.enums.VehicleType;

import java.util.List;

public class Car extends MovingObject {

    public Car(int agentId, List<RouteNode> displayRoute, List<RouteNode> uniformRoute, ITimeProvider timeProvider) {
        super(timeProvider, agentId, 75, uniformRoute, displayRoute);
    }

    Car(Car car) {
        super(car.timeProvider, car.agentId, car.speed, car.uniformRoute, car.simpleRoute);
    }

    @Override
    public String getVehicleType() {
        return VehicleType.REGULAR_CAR.toString();
    }
}
