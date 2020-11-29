package web.message.payloads.infos.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;
import web.message.payloads.models.UpdateDto;

public class BatchedUpdateInfo extends AbstractPayload {
    @JsonProperty("carUpdates")
    public final UpdateDto[] carUpdates;
    @JsonProperty("bikeUpdates")
    public final UpdateDto[] bikeUpdates;
    @JsonProperty("busUpdates")
    public final UpdateDto[] busUpdates;
    @JsonProperty("pedUpdates")
    public final UpdateDto[] pedUpdates;

    public BatchedUpdateInfo(UpdateDto[] carUpdates,
                             UpdateDto[] bikeUpdates,
                             UpdateDto[] busUpdates,
                             UpdateDto[] pedUpdates) {
        this.carUpdates = carUpdates;
        this.bikeUpdates = bikeUpdates;
        this.busUpdates = busUpdates;
        this.pedUpdates = pedUpdates;
    }
}
