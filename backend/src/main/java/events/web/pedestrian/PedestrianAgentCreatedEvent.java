package events.web.pedestrian;

import routing.core.IGeoPosition;
import routing.nodes.RouteNode;

import java.util.List;

public class PedestrianAgentCreatedEvent {
    public final int id;
    public final IGeoPosition position;
    public final List<RouteNode> routeToStation;
    public final List<RouteNode> routeFromStation;
    public final boolean isTestPedestrian;

    public PedestrianAgentCreatedEvent(int id,
                                       IGeoPosition position,
                                       List<RouteNode> routeToStation,
                                       List<RouteNode> routeFromStation,
                                       boolean isTestPedestrian) {
        this.id = id;
        this.position = position;
        this.routeFromStation = routeFromStation;
        this.routeToStation = routeToStation;
        this.isTestPedestrian = isTestPedestrian;
    }
}
