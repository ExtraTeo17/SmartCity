package events.web.vehicle;

import events.web.abstractions.AbstractAgentDeadWithTravelDataEvent;

import javax.annotation.Nullable;

public class VehicleAgentDeadEvent extends AbstractAgentDeadWithTravelDataEvent {
    public VehicleAgentDeadEvent(int id, int travelDistance, @Nullable Long travelTime) {
        super(id, travelDistance, travelTime);
    }
}
