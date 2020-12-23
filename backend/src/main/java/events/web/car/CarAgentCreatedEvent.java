package events.web.car;

import routing.core.IGeoPosition;
import routing.nodes.RouteNode;

import java.util.List;

public class CarAgentCreatedEvent {
    public final int agentId;
    public final IGeoPosition agentPosition;
    public final List<RouteNode> route;
    public final boolean isTestCar;

    public CarAgentCreatedEvent(int agentId,
                                IGeoPosition agentPosition,
                                List<RouteNode> route,
                                boolean isTestCar) {
        this.agentId = agentId;
        this.agentPosition = agentPosition;
        this.route = route;
        this.isTestCar = isTestCar;
    }
}
