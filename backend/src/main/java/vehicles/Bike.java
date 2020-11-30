package vehicles;

import routing.nodes.RouteNode;
import smartcity.ITimeProvider;
import vehicles.enums.VehicleType;

import java.util.List;

import static vehicles.Constants.SPEED_SCALE;

public class Bike extends MovingObject {
    public static final int DEFAULT_SPEED = (int)(10 * SPEED_SCALE);

    public Bike(int agentId, List<RouteNode> displayRoute, List<RouteNode> uniformRoute, ITimeProvider timeProvider) {
        super(timeProvider, agentId, DEFAULT_SPEED, uniformRoute, displayRoute);
    }

    Bike(Bike bike) {
        super(bike.timeProvider, bike.agentId, bike.speed, bike.uniformRoute, bike.simpleRoute);
    }

    @Override
    public String getVehicleType() {
        return VehicleType.BIKE.toString();
    }
}
