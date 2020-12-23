package events.web.car;

import events.web.abstractions.AbstractAgentDeadWithTravelDataEvent;

import javax.annotation.Nullable;

public class CarAgentDeadEvent extends AbstractAgentDeadWithTravelDataEvent {
    public CarAgentDeadEvent(int id, int travelDistance, @Nullable Long travelTime) {
        super(id, travelDistance, travelTime);
    }
}
