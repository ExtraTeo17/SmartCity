package events.web;

import routing.core.IGeoPosition;

public class TroublePointCreatedEvent {
    public final int id;
    public final IGeoPosition position;

    public TroublePointCreatedEvent(int id, IGeoPosition position) {
        this.id = id;
        this.position = position;
    }
}
