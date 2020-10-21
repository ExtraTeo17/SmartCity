package web.message.payloads.infos.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.Location;

public class CreatePedestrianInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;
    @JsonProperty("location")
    private final Location location;
    @JsonProperty("routeFromStation")
    private final Location[] routeFromStation;
    @JsonProperty("routeToStation")
    private final Location[] routeToStation;
    @JsonProperty("isTestPedestrian")
    private final boolean isTestPedestrian;

    public CreatePedestrianInfo(int id,
                                Location location,
                                Location[] routeFromStation,
                                Location[] routeToStation,
                                boolean isTestCar) {
        this.id = id;
        this.location = location;
        this.routeFromStation = routeFromStation;
        this.routeToStation = routeToStation;
        this.isTestPedestrian = isTestCar;
    }
}
