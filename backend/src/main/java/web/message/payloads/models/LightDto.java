package web.message.payloads.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LightDto {
    @JsonProperty("lightGroupId")
    public final long lightGroupId;
    @JsonProperty("location")
    public final Location location;

    public LightDto(long lightGroupId, Location location) {
        this.lightGroupId = lightGroupId;
        this.location = location;
    }
}
