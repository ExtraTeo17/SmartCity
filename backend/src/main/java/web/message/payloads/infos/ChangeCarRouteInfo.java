package web.message.payloads.infos;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.Location;

public class ChangeCarRouteInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;
    @JsonProperty("route")
    private final Location[] newRoute;
    @JsonProperty("location")
    private final Location changeLocation;

    public ChangeCarRouteInfo(int id, Location[] newRoute, Location changeLocation) {
        this.id = id;
        this.newRoute = newRoute;
        this.changeLocation = changeLocation;
    }
}
