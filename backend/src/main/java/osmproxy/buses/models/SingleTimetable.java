package osmproxy.buses.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;

public class SingleTimetable {
    public final int brigadeNr;
    public final LocalTime timeOnStop;
    public final String direction;
    public final String path;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SingleTimetable(@JsonProperty("brygada")
                                       int brigadeNr,
                           @JsonProperty("czas") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "kk:mm:ss")
                                   LocalTime timeOnStop,
                           @JsonProperty("kierunek")
                                       String direction,
                           @JsonProperty("trasa")
                                       String path) {
        this.brigadeNr = brigadeNr;
        this.timeOnStop = timeOnStop;
        this.direction = direction;
        this.path = path;
    }
}
