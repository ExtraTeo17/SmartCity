package vehicles;

import routing.nodes.RouteNode;
import smartcity.ITimeProvider;
import vehicles.enums.VehicleType;

import java.util.List;

public class Bike extends MovingObject {
    public Bike(int agentId, List<RouteNode> displayRoute, List<RouteNode> uniformRoute, ITimeProvider timeProvider) {
        super(timeProvider, agentId, 10, uniformRoute, displayRoute); // TODO: Unify with bike speed used in PedestrianAgent
    }

    Bike(Bike bike) {
        super(bike.timeProvider, bike.agentId, bike.speed, bike.uniformRoute, bike.simpleRoute);
    }

    @Override
    public String getVehicleType() {
        return VehicleType.BIKE.toString();
    }
}
