package events.web.pedestrian;

import routing.core.IGeoPosition;

public class PedestrianAgentUpdatedEvent {
    public final int id;
    public final IGeoPosition position;

    public PedestrianAgentUpdatedEvent(int id, IGeoPosition position) {
        this.id = id;
        this.position = position;
    }
}
