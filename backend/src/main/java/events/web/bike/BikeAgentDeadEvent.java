package events.web.bike;

import events.web.abstractions.AbstractAgentDeadWithTravelDataEvent;

import javax.annotation.Nullable;

public class BikeAgentDeadEvent extends AbstractAgentDeadWithTravelDataEvent {
    public BikeAgentDeadEvent(int id, int travelDistance, @Nullable Long travelTime) {
        super(id, travelDistance, travelTime);
    }
}
