package events.web.bike;

import routing.core.IGeoPosition;
import routing.nodes.RouteNode;

import java.util.List;

public class BikeAgentCreatedEvent {
    public final int agentId;
    public final IGeoPosition agentPosition;
    public final List<RouteNode> route;
    public final boolean isTestBike;

    public BikeAgentCreatedEvent(int agentId,
                                 IGeoPosition agentPosition,
                                 List<RouteNode> route,
                                 boolean isTestBike) {
        this.agentId = agentId;
        this.agentPosition = agentPosition;
        this.route = route;
        this.isTestBike = isTestBike;
    }
}
