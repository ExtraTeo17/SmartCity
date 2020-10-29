package web.message.payloads.infos.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.Location;

public class CreateBikeInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;
    @JsonProperty("location")
    private final Location location;
    @JsonProperty("route")
    private final Location[] route;
    @JsonProperty("isTestBike")
    private final boolean isTestBike;

    public CreateBikeInfo(int id,
                          Location location,
                          Location[] route,
                          boolean isTestBike) {
        this.id = id;
        this.location = location;
        this.route = route;
        this.isTestBike = isTestBike;
    }
}
