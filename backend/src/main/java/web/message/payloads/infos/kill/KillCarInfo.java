package web.message.payloads.infos.kill;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public class KillCarInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;
    @JsonProperty("travelDistance")
    private final int travelDistance;
    @JsonProperty("travelTime")
    private final Long travelTime;

    public KillCarInfo(int id, int travelDistance, Long travelTime) {
        this.id = id;
        this.travelDistance = travelDistance;
        this.travelTime = travelTime;
    }
}
