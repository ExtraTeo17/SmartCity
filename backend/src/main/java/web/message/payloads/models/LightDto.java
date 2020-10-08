package web.message.payloads.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LightDto {
    @JsonProperty("groupId")
    private final long groupId;
    @JsonProperty("location")
    private final Location location;
    @JsonProperty("color")
    private final LightColorDto color;

    public LightDto(long groupId,
                    Location location,
                    LightColorDto color) {
        this.groupId = groupId;
        this.location = location;
        this.color = color;
    }
}
