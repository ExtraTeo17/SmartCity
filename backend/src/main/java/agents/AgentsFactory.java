package agents;

import com.google.inject.Inject;
import routing.RouteNode;
import vehicles.MovingObjectImpl;
import vehicles.TestCar;

import java.util.List;

public class AgentsFactory implements agents.abstractions.IAgentsFactory {
    private final IdGenerator idGenerator;

    @Inject
    public AgentsFactory(IdGenerator idGenerator) {this.idGenerator = idGenerator;}

    @Override
    public VehicleAgent create(List<RouteNode> route, boolean testCar) {
        MovingObjectImpl car = testCar ? new TestCar(route) : new MovingObjectImpl(route);
        return new VehicleAgent(idGenerator.get(VehicleAgent.class), car);
    }

    @Override
    public VehicleAgent create(List<RouteNode> route) {
        return create(route, false);
    }
}
