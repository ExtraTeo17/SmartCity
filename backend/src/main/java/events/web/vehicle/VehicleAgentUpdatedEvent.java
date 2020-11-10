package events.web.vehicle;

import routing.core.IGeoPosition;

public class VehicleAgentUpdatedEvent {
    public final int id;
    public final IGeoPosition position;

    public VehicleAgentUpdatedEvent(int id,
                                    IGeoPosition position) {
        this.id = id;
        this.position = position;
    }
}
