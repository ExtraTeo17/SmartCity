package events.web;

import routing.core.IGeoPosition;

public class TroublePointCreatedEvent {
    public final IGeoPosition position;

    public TroublePointCreatedEvent(IGeoPosition position) {this.position = position;}
}
