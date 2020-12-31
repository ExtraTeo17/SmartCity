package events.web;

import java.time.LocalDateTime;

@SuppressWarnings("ClassWithTooManyFields")
public class StartSimulationEvent {
    public final boolean shouldGenerateCars;
    public final int carsNum;
    public final int testCarId;
    public final boolean shouldGenerateBatchesForCars;

    public final boolean shouldGenerateBikes;
    public final int bikesNum;
    public final int testBikeId;

    public final int pedestriansLimit;
    public final int testPedestrianId;

    public final boolean shouldGenerateTroublePoints;
    public final int timeBeforeTrouble;

    public final boolean shouldGenerateBusFailures;
    public final boolean shouldDetectTrafficJams;

    public final boolean useFixedRoutes;
    public final boolean useFixedTroublePoints;

    public final LocalDateTime startTime;
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

    public StartSimulationEvent(boolean shouldGenerateCars,
                                int carsNum,
                                int testCarId,
                                boolean shouldGenerateBatchesForCars,
                                boolean shouldGenerateBikes,
                                int bikesNum,
                                int testBikeId,
                                int pedestriansLimit,
                                int testPedestrianId,
                                boolean shouldGenerateTroublePoints,
                                int timeBeforeTrouble,
                                boolean shouldGenerateBusFailures,
                                boolean shouldDetectTrafficJams,
                                boolean useFixedRoutes,
                                boolean useFixedTroublePoints,
                                LocalDateTime startTime,
                                int timeScale,
                                boolean lightStrategyActive,
                                int extendLightTime,
                                boolean stationStrategyActive,
                                int extendWaitTime,
                                boolean troublePointStrategyActive,
                                int troublePointThresholdUntilIndexChange,
                                int noTroublePointStrategyIndexFactor,
                                boolean trafficJamStrategyActive,
                                boolean transportChangeStrategyActive) {

        this.shouldGenerateCars = shouldGenerateCars;
        this.carsNum = carsNum;
        this.testCarId = testCarId;
        this.shouldGenerateBatchesForCars = shouldGenerateBatchesForCars;
        this.shouldGenerateBikes = shouldGenerateBikes;
        this.bikesNum = bikesNum;
        this.testBikeId = testBikeId;
        this.pedestriansLimit = pedestriansLimit;
        this.testPedestrianId = testPedestrianId;
        this.shouldGenerateTroublePoints = shouldGenerateTroublePoints;
        this.timeBeforeTrouble = timeBeforeTrouble;
        this.shouldGenerateBusFailures = shouldGenerateBusFailures;
        this.shouldDetectTrafficJams = shouldDetectTrafficJams;
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
