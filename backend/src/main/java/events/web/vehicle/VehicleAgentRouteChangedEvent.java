package events.web.vehicle;

import routing.core.IGeoPosition;

import java.util.List;

public class VehicleAgentRouteChangedEvent {
    public final int agentId;
    public final List<? extends IGeoPosition> routeStart;
    public final IGeoPosition changePosition;
    public final List<? extends IGeoPosition> routeEnd;

    public VehicleAgentRouteChangedEvent(int agentId,
                                         List<? extends IGeoPosition> routeStart,
                                         IGeoPosition changePosition,
                                         List<? extends IGeoPosition> routeEnd) {
        this.agentId = agentId;
        this.changePosition = changePosition;
        this.routeEnd = routeEnd;
        this.routeStart = routeStart;
    }
}