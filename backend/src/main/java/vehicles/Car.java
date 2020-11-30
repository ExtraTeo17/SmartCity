package vehicles;

import routing.nodes.RouteNode;
import smartcity.ITimeProvider;
import vehicles.enums.VehicleType;

import java.util.List;

import static vehicles.Constants.SPEED_SCALE;

public class Car extends MovingObject {
    private static final int DEFAULT_SPEED =  (int)(35 * SPEED_SCALE);

    public Car(int agentId, List<RouteNode> displayRoute, List<RouteNode> uniformRoute, ITimeProvider timeProvider) {
        super(timeProvider, agentId, DEFAULT_SPEED, uniformRoute, displayRoute);
    }

    Car(Car car) {
        super(car.timeProvider, car.agentId, car.speed, car.uniformRoute, car.simpleRoute);
    }

    @Override
    public String getVehicleType() {
        return VehicleType.REGULAR_CAR.toString();
    }
}
