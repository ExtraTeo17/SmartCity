package web.message.payloads.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.LightDto;
import web.message.payloads.models.Location;

public class PrepareResponse extends AbstractPayload {
    @JsonProperty("lights")
    private final LightDto[] lights;

    public PrepareResponse(LightDto[] lights) {
        this.lights = lights;
    }
}
