package web.message.payloads.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public class StartResponse extends AbstractPayload {
    @JsonProperty("timeScale")
    private final int timeScale;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public StartResponse(@JsonProperty("timeScale") int timeScale) {
        this.timeScale = timeScale;
    }
}
