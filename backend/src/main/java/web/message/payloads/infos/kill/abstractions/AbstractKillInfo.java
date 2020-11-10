package web.message.payloads.infos.kill.abstractions;

import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public abstract class AbstractKillInfo extends AbstractPayload {
    @JsonProperty("id")
    private final int id;

    protected AbstractKillInfo(int id) {this.id = id;}
}
