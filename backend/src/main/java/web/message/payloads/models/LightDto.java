package web.message.payloads.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LightDto {
    @JsonProperty("id")
    private final long id;
    @JsonProperty("groupId")
    private final long groupId;
    @JsonProperty("location")
    private final Location location;
    @JsonProperty("color")
    private final LightColorDto color;

    public LightDto(long id,
                    long groupId,
                    Location location,
                    LightColorDto color) {
        this.id = id;
        this.groupId = groupId;
        this.location = location;
        this.color = color;
    }
}
