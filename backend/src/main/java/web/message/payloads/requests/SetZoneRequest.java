package web.message.payloads.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SetZoneRequest {
    public final double latitude;
    public final double longitude;
    public final double radius;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SetZoneRequest(@JsonProperty("latitude") double latitude,
                          @JsonProperty("longitude") double longitude,
                          @JsonProperty("radius") double radius) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }
}
