package events.web.pedestrian;

import events.web.abstractions.AbstractAgentDeadWithTravelDataEvent;

import javax.annotation.Nullable;

public class PedestrianAgentDeadEvent extends AbstractAgentDeadWithTravelDataEvent {
    public PedestrianAgentDeadEvent(int id, int travelDistance, @Nullable Long travelTime) {
        super(id, travelDistance, travelTime);
    }
}
