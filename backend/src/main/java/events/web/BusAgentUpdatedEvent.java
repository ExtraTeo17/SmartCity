package events.web;

import routing.core.IGeoPosition;

public class BusAgentUpdatedEvent {
    public final int id;
    public final IGeoPosition position;

    public BusAgentUpdatedEvent(int id, IGeoPosition position) {
        this.id = id;
        this.position = position;
    }
}
