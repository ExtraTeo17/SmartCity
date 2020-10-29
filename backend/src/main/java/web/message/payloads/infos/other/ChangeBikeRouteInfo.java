package web.message.payloads.infos.other;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.Location;

public class ChangeBikeRouteInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;
    @JsonProperty("routeStart")
    private final Location[] routeStart;
    @JsonProperty("routeEnd")
    private final Location[] routeEnd;
    @JsonProperty("location")
    private final Location changeLocation;

    public ChangeBikeRouteInfo(int id, Location[] routeStart, Location changeLocation, Location[] routeEnd) {
        this.id = id;
        this.routeStart = routeStart;
        this.routeEnd = routeEnd;
        this.changeLocation = changeLocation;
    }
}
