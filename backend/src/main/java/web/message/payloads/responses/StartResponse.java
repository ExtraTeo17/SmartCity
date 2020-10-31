package web.message.payloads.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public class StartResponse extends AbstractPayload {
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public StartResponse() {
    }
}
