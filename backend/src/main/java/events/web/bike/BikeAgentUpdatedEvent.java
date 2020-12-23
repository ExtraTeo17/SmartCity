package events.web.bike;

import routing.core.IGeoPosition;

public class BikeAgentUpdatedEvent {
    public final int id;
    public final IGeoPosition position;

    public BikeAgentUpdatedEvent(int id,
                                 IGeoPosition position) {
        this.id = id;
        this.position = position;
    }
}
