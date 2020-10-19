package events.web;

import vehicles.enums.BusFillState;

public class BusAgentFillStateUpdatedEvent {
    public final int id;
    public final BusFillState fillState;

    public BusAgentFillStateUpdatedEvent(int id, BusFillState fillState) {
        this.id = id;
        this.fillState = fillState;
    }
}
