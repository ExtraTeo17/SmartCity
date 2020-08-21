package events;

import routing.IGeoPosition;

public class VehicleAgentCreatedEvent {
    public final IGeoPosition agentPosition;

    public VehicleAgentCreatedEvent(IGeoPosition agentPosition) {
        this.agentPosition = agentPosition;
    }
}
