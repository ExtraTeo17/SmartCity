package events.web;

import events.web.models.UpdateObject;

import java.util.List;

public class BatchedUpdateEvent {
    public final List<UpdateObject> carUpdates;
    public final List<UpdateObject> bikeUpdates;
    public final List<UpdateObject> busUpdates;
    public final List<UpdateObject> pedUpdates;

    public BatchedUpdateEvent(List<UpdateObject> carUpdates,
                              List<UpdateObject> bikeUpdates,
                              List<UpdateObject> busUpdates,
                              List<UpdateObject> pedUpdates) {
        this.carUpdates = carUpdates;
        this.bikeUpdates = bikeUpdates;
        this.busUpdates = busUpdates;
        this.pedUpdates = pedUpdates;
    }
}
