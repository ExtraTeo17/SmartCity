package web.message.payloads.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Location {
    @JsonProperty("lat")
    public final double latitude;
    @JsonProperty("lng")
    public final double longitude;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Location(@JsonProperty("lat") double latitude,
                    @JsonProperty("lng") double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
