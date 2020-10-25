package events.web.pedestrian;

import routing.core.IGeoPosition;

public class PedestrianAgentLeftBusEvent {
    public final int id;
    public final IGeoPosition position;

    public PedestrianAgentLeftBusEvent(int id, IGeoPosition position) {
        this.id = id;
        this.position = position;
    }
}
