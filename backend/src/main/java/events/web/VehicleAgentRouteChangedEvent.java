package events.web;

import routing.core.IGeoPosition;
import routing.nodes.RouteNode;

import java.util.List;

public class VehicleAgentRouteChangedEvent {
    public final int agentId;
    public final IGeoPosition changePosition;
    public final List<RouteNode> route;

    public VehicleAgentRouteChangedEvent(int agentId, List<RouteNode> route, IGeoPosition changePosition) {
        this.agentId = agentId;
        this.changePosition = changePosition;
        this.route = route;
    }
}
