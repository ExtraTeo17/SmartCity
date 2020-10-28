package web.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@SuppressWarnings("ClassWithTooManyFields")
public enum MessageType {
    PREPARE_SIMULATION_REQUEST(1),
    PREPARE_SIMULATION_RESPONSE(2),

    START_SIMULATION_REQUEST(3),
    START_SIMULATION_RESPONSE(4),

    DEBUG_REQUEST(5),

    CREATE_CAR_INFO(10),
    UPDATE_CAR_INFO(11),
    KILL_CAR_INFO(12),
    UPDATE_CAR_ROUTE_INFO(13),

    SWITCH_LIGHTS_INFO(20),
    CREATE_TROUBLE_POINT_INFO(21),
    HIDE_TROUBLE_POINT_INFO(22),
    START_TRAFFIC_JAM_INFO(23),
    END_TRAFFIC_JAM_INFO(24),

    UPDATE_BUS_INFO(30),
    UPDATE_BUS_FILL_STATE_INFO(31),
    KILL_BUS_INFO(32),

    CREATE_PEDESTRIAN_INFO(40),
    UPDATE_PEDESTRIAN_INFO(41),
    PUSH_PEDESTRIAN_INTO_BUS_INFO(42),
    PULL_PEDESTRIAN_FROM_BUS_INFO(43),
    KILL_PEDESTRIAN_INFO(44);

    private final int code;

    MessageType(int code) {
        this.code = code;
    }

    @JsonCreator
    public static MessageType create(int code) {
        for (var value : MessageType.values()) {
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
