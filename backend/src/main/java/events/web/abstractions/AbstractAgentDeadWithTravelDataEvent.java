package events.web.abstractions;

import javax.annotation.Nullable;

public abstract class AbstractAgentDeadWithTravelDataEvent extends AbstractAgentDeadEvent {
    public final int travelDistance;
    @Nullable
    public final Long travelTime;

    protected AbstractAgentDeadWithTravelDataEvent(int id, int travelDistance, @Nullable Long travelTime) {
        super(id);
        this.travelDistance = travelDistance;
        this.travelTime = travelTime;
    }
}
