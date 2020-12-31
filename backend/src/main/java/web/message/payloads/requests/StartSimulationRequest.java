package web.message.payloads.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import web.message.payloads.AbstractPayload;

import java.time.ZonedDateTime;

@SuppressWarnings("ClassWithTooManyFields")
public class StartSimulationRequest extends AbstractPayload {

    public final boolean generateCars;
    public final int carsLimit;
    public final int testCarId;
    public final boolean generateBatchesForCars;

    public final boolean generateBikes;
    public final int bikesLimit;
    public final int testBikeId;

    public final int pedestriansLimit;
    public final int testPedestrianId;

    public final boolean generateTroublePoints;
    public final int timeBeforeTrouble;

    public final boolean detectTrafficJams;
    public final boolean generateBusFailures;

    public final boolean useFixedRoutes;
    public final boolean useFixedTroublePoints;

    public final ZonedDateTime startTime;
    public final int timeScale;

    public final boolean lightStrategyActive;
    public final int extendLightTime;

    public final boolean stationStrategyActive;
    public final int extendWaitTime;

    public final boolean troublePointStrategyActive;
    public final int troublePointThresholdUntilIndexChange;
    public final int noTroublePointStrategyIndexFactor;

    public final boolean trafficJamStrategyActive;
    public final boolean transportChangeStrategyActive;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public StartSimulationRequest(@JsonProperty("generateCars") boolean generateCars,
                                  @JsonProperty("carsLimit") int carsLimit,
                                  @JsonProperty("testCarId") int testCarId,
                                  @JsonProperty("generateBatchesForCars") boolean generateBatchesForCars,

                                  @JsonProperty("generateBikes") boolean generateBikes,
                                  @JsonProperty("bikesLimit") int bikesLimit,
                                  @JsonProperty("testBikeId") int testBikeId,

                                  @JsonProperty("pedLimit") int pedestriansLimit,
                                  @JsonProperty("testPedId") int testPedestrianId,

                                  @JsonProperty("generateTroublePoints") boolean generateTroublePoints,
                                  @JsonProperty("timeBeforeTrouble") int timeBeforeTrouble,

                                  @JsonProperty("detectTrafficJams") boolean detectTrafficJams,
                                  @JsonProperty("generateBusFailures") boolean generateBusFailures,

                                  @JsonProperty("useFixedRoutes") boolean useFixedRoutes,
                                  @JsonProperty("useFixedTroublePoints") boolean useFixedTroublePoints,

                                  @JsonProperty("startTime") ZonedDateTime startTime,
                                  @JsonProperty("timeScale") int timeScale,

                                  @JsonProperty("lightStrategyActive") boolean lightStrategyActive,
                                  @JsonProperty("extendLightTime") int extendLightTime,

                                  @JsonProperty("stationStrategyActive") boolean stationStrategyActive,
                                  @JsonProperty("extendWaitTime") int extendWaitTime,

                                  @JsonProperty("troublePointStrategyActive") boolean troublePointStrategyActive,
                                  @JsonProperty("troublePointThresholdUntilIndexChange") int troublePointThresholdUntilIndexChange,
                                  @JsonProperty("noTroublePointStrategyIndexFactor") int noTroublePointStrategyIndexFactor,

                                  @JsonProperty("trafficJamStrategyActive") boolean trafficJamStrategyActive,
                                  @JsonProperty("transportChangeStrategyActive") boolean transportChangeStrategyActive) {

        this.generateCars = generateCars;
        this.carsLimit = carsLimit;
        this.testCarId = testCarId;
        this.generateBatchesForCars = generateBatchesForCars;

        this.generateBikes = generateBikes;
        this.bikesLimit = bikesLimit;
        this.testBikeId = testBikeId;

        this.pedestriansLimit = pedestriansLimit;
        this.testPedestrianId = testPedestrianId;

        this.generateTroublePoints = generateTroublePoints;
        this.timeBeforeTrouble = timeBeforeTrouble;

        this.detectTrafficJams = detectTrafficJams;
        this.generateBusFailures = generateBusFailures;

        this.useFixedRoutes = useFixedRoutes;
        this.useFixedTroublePoints = useFixedTroublePoints;

        this.startTime = startTime;
        this.timeScale = timeScale;

        this.lightStrategyActive = lightStrategyActive;
        this.extendLightTime = extendLightTime;

        this.stationStrategyActive = stationStrategyActive;
        this.extendWaitTime = extendWaitTime;

        this.troublePointStrategyActive = troublePointStrategyActive;
        this.troublePointThresholdUntilIndexChange = troublePointThresholdUntilIndexChange;
        this.noTroublePointStrategyIndexFactor = noTroublePointStrategyIndexFactor;

        this.trafficJamStrategyActive = trafficJamStrategyActive;
        this.transportChangeStrategyActive = transportChangeStrategyActive;
    }
}
