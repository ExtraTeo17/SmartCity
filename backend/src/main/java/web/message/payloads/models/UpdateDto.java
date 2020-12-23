package web.message.payloads.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateDto {
    @JsonProperty("id")
    private final int id;
    @JsonProperty("location")
    private final Location location;

    public UpdateDto(int id, Location location) {
        this.id = id;
        this.location = location;
    }
}
