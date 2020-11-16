package web.message.payloads.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public class StartSimulationRequest extends AbstractPayload {
    public final int carsNum;
    public final int testCarId;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public StartSimulationRequest(@JsonProperty("carsNum") int carsNum,
                                  @JsonProperty("testCarId") int testCarId) {
        this.carsNum = carsNum;
        this.testCarId = testCarId;
    }
}
