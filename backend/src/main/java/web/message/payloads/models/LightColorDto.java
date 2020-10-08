package web.message.payloads.models;

import agents.utilities.LightColor;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import web.message.MessageType;

public  enum LightColorDto {
    GREEN(0),
    YELLOW(1),
    RED(2);

    private final int code;

    LightColorDto(int code) {
        this.code = code;
    }

    @JsonCreator
    public static LightColorDto create(int code) {
        for (var value : LightColorDto.values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new IllegalArgumentException("Message type with code: " + code + "was not found");
    }

    @JsonValue
    public int getCode() {
        return code;
    }
}
