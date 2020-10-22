package web.message.payloads.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.BusDto;
import web.message.payloads.models.LightDto;
import web.message.payloads.models.StationDto;

public class PrepareResponse extends AbstractPayload {
    @JsonProperty("lights")
    private final LightDto[] lights;
    @JsonProperty("stations")
    private final StationDto[] stations;
    @JsonProperty("buses")
    private final BusDto[] buses;

    public PrepareResponse(LightDto[] lights,
                           StationDto[] stations,
                           BusDto[] buses) {
        this.lights = lights;
        this.stations = stations;
        this.buses = buses;
    }
}
