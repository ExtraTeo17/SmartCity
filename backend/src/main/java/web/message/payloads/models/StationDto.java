package web.message.payloads.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StationDto {
    @JsonProperty("id")
    private final long id;
    @JsonProperty("location")
    private final Location location;

    public StationDto(long id, Location location) {
      this.id = id;
      this.location = location;
    }
}
