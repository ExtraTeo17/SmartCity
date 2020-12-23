package web.message.payloads.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Location {
    @JsonProperty("lat")
    public final double latitude;
    @JsonProperty("lng")
    public final double longitude;

    public Location(double latitude,
                    double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
