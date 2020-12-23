package web.message.payloads.infos.other;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public class CrashBusInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;

    public CrashBusInfo(int id) {
        this.id = id;
    }
}
