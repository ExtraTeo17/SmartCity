package web.message.payloads.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.Location;

public class PrepareResponse extends AbstractPayload {
    @JsonProperty("locations")
    private final Location[] locations;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PrepareResponse(@JsonProperty("locations") Location[] locations) {
        this.locations = locations;
    }
}
