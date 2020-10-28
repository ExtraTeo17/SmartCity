package web.message.payloads.infos.kill;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public class EndTrafficJamInfo extends AbstractPayload {
    @JsonProperty("lightId")
    private final int lightId;

    public EndTrafficJamInfo(int id) {
        this.lightId = id;
    }
}
