package web.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Main message class.
 */
public class MessageDto {
    public final MessageType type;
    public final String payload;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MessageDto(@JsonProperty("type") MessageType type,
                      @JsonProperty("payload") String payload) {
        this.type = type;
        this.payload = payload;
    }
}
