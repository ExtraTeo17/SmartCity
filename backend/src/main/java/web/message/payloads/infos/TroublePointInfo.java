package web.message.payloads.infos;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.Location;

public class TroublePointInfo extends AbstractPayload {
    @JsonProperty("location")
    private final Location location;

    public TroublePointInfo(Location location) {

        this.location = location;

    }
}
