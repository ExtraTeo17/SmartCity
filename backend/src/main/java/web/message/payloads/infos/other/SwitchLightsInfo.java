package web.message.payloads.infos.other;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public class SwitchLightsInfo extends AbstractPayload {
    @JsonProperty("lightGroupId")
    private final long lightGroupId;

    public SwitchLightsInfo(long lightGroupId) {
        this.lightGroupId = lightGroupId;
    }
}
