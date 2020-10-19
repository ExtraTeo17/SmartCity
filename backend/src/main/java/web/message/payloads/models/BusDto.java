package web.message.payloads.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BusDto {
    @JsonProperty("id")
    private final int id;
    @JsonProperty("location")
    private final Location location;
    @JsonProperty("route")
    private final Location[] route;
    @JsonProperty("fillState")
    private final BusFillStateDto fillState;

    public BusDto(int id, Location location,
                  Location[] route,
                  BusFillStateDto fillState) {
        this.id = id;
        this.location = location;
        this.route = route;
        this.fillState = fillState;
    }
}
