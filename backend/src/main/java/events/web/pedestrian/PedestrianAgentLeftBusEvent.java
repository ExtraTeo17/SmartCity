package events.web.pedestrian;

import routing.core.IGeoPosition;

public class PedestrianAgentLeftBusEvent {
    public final int id;
    public final IGeoPosition position;
    public final boolean shouldShowRoute;

    public PedestrianAgentLeftBusEvent(int id, IGeoPosition position, boolean shouldShowRoute) {
        this.id = id;
        this.position = position;
        this.shouldShowRoute = shouldShowRoute;
    }
}
