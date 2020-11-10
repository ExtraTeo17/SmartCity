package web.message.payloads.infos.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.Location;

public class UpdatePedestrianInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;
    @JsonProperty("location")
    private final Location location;

    public UpdatePedestrianInfo(int id, Location location) {
        this.id = id;
        this.location = location;
    }
}
