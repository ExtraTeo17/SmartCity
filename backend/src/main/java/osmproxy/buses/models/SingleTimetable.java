package osmproxy.buses.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import osmproxy.buses.serialization.WrappingDateTimeDeserializer;

import java.time.LocalDateTime;

public class SingleTimetable {
    public final String brigadeId;
    public final LocalDateTime timeOnStop;
    public final String direction;
    public final String path;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SingleTimetable(@JsonProperty("brygada")
                                   String brigadeId,
                           @JsonProperty("czas") @JsonDeserialize(using = WrappingDateTimeDeserializer.class)
                                       LocalDateTime timeOnStop,
                           @JsonProperty("kierunek")
                                   String direction,
                           @JsonProperty("trasa")
                                   String path) {
        this.brigadeId = brigadeId;
        this.timeOnStop = timeOnStop;
        this.direction = direction;
        this.path = path;
    }
}
