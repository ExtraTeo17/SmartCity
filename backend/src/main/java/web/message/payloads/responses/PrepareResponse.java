package web.message.payloads.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.LightDto;
import web.message.payloads.models.StationDto;

public class PrepareResponse extends AbstractPayload {
    @JsonProperty("lights")
    private final LightDto[] lights;
    @JsonProperty("stations")
    private final StationDto[] stations;

    public PrepareResponse(LightDto[] lights,
                           StationDto[] stations) {
        this.lights = lights;
        this.stations = stations;
    }
}
