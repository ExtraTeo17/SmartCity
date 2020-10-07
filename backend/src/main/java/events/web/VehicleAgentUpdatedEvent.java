package events.web;

import routing.core.IGeoPosition;

public class VehicleAgentUpdatedEvent {
    public final int agentId;
    public final IGeoPosition agentPosition;

    public VehicleAgentUpdatedEvent(int agentId,
                                    IGeoPosition agentPosition) {
        this.agentId = agentId;
        this.agentPosition = agentPosition;
    }
}
