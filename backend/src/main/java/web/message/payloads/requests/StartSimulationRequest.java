package web.message.payloads.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public class StartSimulationRequest extends AbstractPayload {
    public final int carsNum;
    public final int testCarId;
    public final boolean generateCars;
    public final boolean generatePedestrians;
    public final boolean generateTroublePoints ;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public StartSimulationRequest(@JsonProperty("carsNum") int carsNum,
                                  @JsonProperty("testCarId") int testCarId,
                                  @JsonProperty("generateCars") boolean generateCars,
                                  @JsonProperty("generatePedestrians") boolean generatePedestrians,
                                  @JsonProperty("generateTroublePoints") boolean generateTroublePoints) {
        this.carsNum = carsNum;
        this.testCarId = testCarId;
        this.generatePedestrians = generatePedestrians;
        this.generateCars = generateCars;
        this.generateTroublePoints = generateTroublePoints;
    }
}
