package web.message.payloads.infos.kill;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public class KillPedestrianInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;

    public KillPedestrianInfo(int id) {this.id = id;}
}
