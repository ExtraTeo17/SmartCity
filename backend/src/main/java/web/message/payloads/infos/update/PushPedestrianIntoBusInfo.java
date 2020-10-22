package web.message.payloads.infos.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public class PushPedestrianIntoBusInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;

    public PushPedestrianIntoBusInfo(int id) {
        this.id = id;
    }
}
