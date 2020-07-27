package web.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageType {
    SET_ZONE(1),
    ;

    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    @JsonCreator
    public static MessageType create(int code) {
        // TODO: improve
        var values = MessageType.values();
        for (var value : values) {
            if (value.code == code) {
                return value;
            }
        }

        return null;
    }

    @JsonValue
    public int getCode() {
        return code;
    }
}
