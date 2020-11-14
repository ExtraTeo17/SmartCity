package events.web.car;

import routing.core.IGeoPosition;

public class CarAgentUpdatedEvent {
    public final int id;
    public final IGeoPosition position;

    public CarAgentUpdatedEvent(int id,
                                IGeoPosition position) {
        this.id = id;
        this.position = position;
    }
}
