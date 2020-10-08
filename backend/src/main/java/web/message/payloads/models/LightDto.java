package web.message.payloads.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LightDto {
    @JsonProperty("lightGroupId")
    public final long lightGroupId;
    @JsonProperty("location")
    public final Location location;
    @JsonProperty("lightColor")
    public final LightColorDto lightColor;

    public LightDto(long lightGroupId,
                    Location location,
                    LightColorDto lightColor) {
        this.lightGroupId = lightGroupId;
        this.location = location;
        this.lightColor = lightColor;
    }
}
