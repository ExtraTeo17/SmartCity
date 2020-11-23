package events.web.models;

import routing.core.IGeoPosition;

public class UpdateObject {
    public final int id;
    public final IGeoPosition position;

    public UpdateObject(int id, IGeoPosition position) {
        this.id = id;
        this.position = position;
    }
}
