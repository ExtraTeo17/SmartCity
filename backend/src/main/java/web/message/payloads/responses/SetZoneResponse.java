package web.message.payloads.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.Location;

public class SetZoneResponse extends AbstractPayload {
    @JsonProperty("locations")
    public final Location[] locations;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SetZoneResponse(@JsonProperty("locations") Location[] locations) {
        this.locations = locations;
    }
}
