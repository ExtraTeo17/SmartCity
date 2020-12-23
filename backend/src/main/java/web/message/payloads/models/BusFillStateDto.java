package web.message.payloads.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BusFillStateDto {
    LOW(0),
    MID(10),
    HIGH(25);

    private final int code;

    BusFillStateDto(int code) {
        this.code = code;
    }

    @JsonCreator
    public static BusFillStateDto create(int code) {
        for (var value : BusFillStateDto.values()) {
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
