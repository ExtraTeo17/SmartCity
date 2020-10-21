package web.message.payloads.infos.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.Location;

public class CreateTroublePointInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;
    @JsonProperty("location")
    private final Location location;

    public CreateTroublePointInfo(int id, Location location) {
        this.id = id;

        this.location = location;

    }
}
