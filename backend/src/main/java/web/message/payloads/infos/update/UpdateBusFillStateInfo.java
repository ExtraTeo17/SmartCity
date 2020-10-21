package web.message.payloads.infos.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.BusFillStateDto;

public class UpdateBusFillStateInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;
    @JsonProperty("fillState")
    private final BusFillStateDto fillState;

    public UpdateBusFillStateInfo(int id, BusFillStateDto fillState) {
        this.id = id;
        this.fillState = fillState;
    }
}
