package web.message.payloads.infos;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.BusFillStateDto;
import web.message.payloads.models.Location;

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
