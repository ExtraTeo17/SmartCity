package events.web;

import events.web.models.UpdateObject;

import java.util.List;

public class BatchedUpdateEvent {
    public final List<UpdateObject> carUpdates;

    public BatchedUpdateEvent(List<UpdateObject> carUpdates) {this.carUpdates = carUpdates;}
}
