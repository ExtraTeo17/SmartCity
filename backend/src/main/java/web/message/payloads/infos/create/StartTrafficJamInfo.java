package web.message.payloads.infos.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public class StartTrafficJamInfo extends AbstractPayload {
    @JsonProperty("lightId")
    private final int lightId;

    public StartTrafficJamInfo(int lightId) {this.lightId = lightId;}
}
