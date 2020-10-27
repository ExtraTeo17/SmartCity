package events.web.vehicle;

import javax.annotation.Nullable;

public class VehicleAgentDeadEvent {
    public final int id;
    public final int travelDistance;
    @Nullable
    public Long travelTime;

    public VehicleAgentDeadEvent(int id, int travelDistance) {
        this.id = id;
        this.travelDistance = travelDistance;
    }

    public VehicleAgentDeadEvent(int id, int travelDistance, @Nullable Long travelTime) {
        this(id, travelDistance);
        this.travelTime = travelTime;
    }
}
