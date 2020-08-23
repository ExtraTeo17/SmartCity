package web.message.payloads.infos;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.Location;

public class CreateCarInfo extends AbstractPayload {
    @JsonProperty("location")
    private final Location location;

    public CreateCarInfo(Location location) {
        this.location = location;
    }
}
