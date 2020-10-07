package web.message.payloads.infos;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.models.LightDto;

public class SwitchLightsInfo {
    @JsonProperty("lightGroupId")
    public final int lightGroupId;

    public SwitchLightsInfo(int lightGroupId) {
        this.lightGroupId = lightGroupId;
    }
}
