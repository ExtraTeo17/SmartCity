package web.message.payloads.infos.kill.abstractions;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractKillInfoWithTravelData extends AbstractKillInfo {
    @JsonProperty("travelDistance")
    private final int travelDistance;
    @JsonProperty("travelTime")
    private final Long travelTime;

    protected AbstractKillInfoWithTravelData(int id, int travelDistance, Long travelTime) {
        super(id);
        this.travelDistance = travelDistance;
        this.travelTime = travelTime;
    }
}
