package web.message.payloads.infos.kill;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public class EndTrafficJamInfo extends AbstractPayload {
    @JsonProperty("lightId")
    private final long lightId;

    public EndTrafficJamInfo(long id) {
        this.lightId = id;
    }
}
