package agents.abstractions;

import agents.VehicleAgent;
import routing.RouteNode;

import java.util.List;

public interface IAgentsFactory {
    VehicleAgent create(List<RouteNode> route, boolean testCar);

    VehicleAgent create(List<RouteNode> route);
}
