package web.message.payloads.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class StartSimulationRequest extends AbstractPayload {
    public final int carsNum;
    public final int testCarId;
    public final boolean generateCars;
    public final boolean generateTroublePoints;
    public final ZonedDateTime startTime;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public StartSimulationRequest(@JsonProperty("carsNum") int carsNum,
                                  @JsonProperty("testCarId") int testCarId,
                                  @JsonProperty("generateCars") boolean generateCars,
                                  @JsonProperty("generateTroublePoints") boolean generateTroublePoints,
                                  @JsonProperty("startTime") ZonedDateTime startTime) {
        this.carsNum = carsNum;
        this.testCarId = testCarId;
        this.generateCars = generateCars;
        this.generateTroublePoints = generateTroublePoints;
        this.startTime = startTime;
    }
}
