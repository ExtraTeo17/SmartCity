package web.message.payloads.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

public class PrepareSimulationRequest extends AbstractPayload {
    public final double latitude;
    public final double longitude;
    public final double radius;
    public final boolean generatePedestrians;
    public final  int pedestriansLimit;
    public final int testPedestrianId;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public PrepareSimulationRequest(@JsonProperty("latitude") double latitude,
                                    @JsonProperty("longitude") double longitude,
                                    @JsonProperty("radius") double radius,
                                    @JsonProperty("generatePedestrians") boolean generatePedestrians,
                                    @JsonProperty("pedLimit")  int pedestriansLimit,
                                    @JsonProperty("testPedId") int testPedestrianId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.generatePedestrians = generatePedestrians;
        this.pedestriansLimit = pedestriansLimit;
        this.testPedestrianId = testPedestrianId;
    }
}
