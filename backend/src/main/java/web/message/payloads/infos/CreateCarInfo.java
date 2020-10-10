package web.message.payloads.infos;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.Location;

public class CreateCarInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;
    @JsonProperty("location")
    private final Location location;
    @JsonProperty("route")
    private final Location[] route;
    @JsonProperty("isTestCar")
    private final boolean isTestCar;

    public CreateCarInfo(int id,
                         Location location,
                         Location[] route,
                         boolean isTestCar) {
        this.id = id;
        this.location = location;
        this.route = route;
        this.isTestCar = isTestCar;
    }
}
