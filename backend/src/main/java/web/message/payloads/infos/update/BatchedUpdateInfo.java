package web.message.payloads.infos.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.UpdateDto;

public class BatchedUpdateInfo extends AbstractPayload {
    @JsonProperty("carUpdates")
    public final UpdateDto[] carUpdates;

    public BatchedUpdateInfo(UpdateDto[] carUpdates) {
        this.carUpdates = carUpdates;
    }
}
