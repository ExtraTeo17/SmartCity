package events.web;

import routing.core.IGeoPosition;

public class VehicleAgentCreatedEvent {
    public final int agentId;
    public final IGeoPosition agentPosition;
    public final boolean isTestCar;

    public VehicleAgentCreatedEvent(int agentId,
                                    IGeoPosition agentPosition,
                                    boolean isTestCar) {
        this.agentId = agentId;
        this.agentPosition = agentPosition;
        this.isTestCar = isTestCar;
    }
}
