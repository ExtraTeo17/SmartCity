package web.message.payloads.infos.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.Location;

public class PullPedestrianInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;
    @JsonProperty("location")
    private final Location location;
    @JsonProperty("showRoute")
    private final boolean showRoute;

    public PullPedestrianInfo(int id, Location location, boolean showRoute) {
        this.id = id;
        this.location = location;
        this.showRoute = showRoute;
    }
}
