package events.web;

import routing.core.IGeoPosition;
import routing.nodes.RouteNode;

import java.util.List;

public class VehicleAgentCreatedEvent {
    public final int agentId;
    public final IGeoPosition agentPosition;
    public final List<RouteNode> route;
    public final boolean isTestCar;

    public VehicleAgentCreatedEvent(int agentId,
                                    IGeoPosition agentPosition,
                                    List<RouteNode> route,
                                    boolean isTestCar) {
        this.agentId = agentId;
        this.agentPosition = agentPosition;
        this.route = route;
        this.isTestCar = isTestCar;
    }
}
