package events.web;

import java.time.LocalDateTime;

@SuppressWarnings("ClassWithTooManyFields")
public class StartSimulationEvent {
    public final boolean shouldGenerateCars;
    public final int carsNum;
    public final int testCarId;

    public final boolean shouldGenerateBikes;
    public final int bikesNum;
    public final int testBikeId;

    public final boolean shouldGenerateTroublePoints;
    public final int timeBeforeTrouble;

    public final int pedestriansLimit;
    public final int testPedestrianId;

    public final boolean useFixedRoutes;
    public final boolean useFixedConstructionSites;
    public final LocalDateTime startTime;
    public final int timeScale;

    public final boolean lightStrategyActive;
    public final int extendLightTime;

    public final boolean stationStrategyActive;
    public final int extendWaitTime;

    public final boolean changeRouteOnTroublePoint;
    public final boolean changeRouteOnTrafficJam;

    public StartSimulationEvent(boolean shouldGenerateCars,
                                int carsNum,
                                int testCarId,
                                boolean shouldGenerateBikes,
                                int bikesNum,
                                int testBikeId,
                                boolean generateTroublePoints,
                                int timeBeforeTrouble,
                                int pedestriansLimit,
                                int testPedestrianId,
                                boolean useFixedRoutes,
                                boolean useFixedConstructionSites,
                                LocalDateTime startTime,
                                int timeScale,
                                boolean lightStrategyActive,
                                int extendLightTime,
                                boolean stationStrategyActive,
                                int extendWaitTime,
                                boolean changeRouteOnTroublePoint,
                                boolean changeRouteOnTrafficJam) {
        this.carsNum = carsNum;
        this.testCarId = testCarId;
        this.shouldGenerateCars = shouldGenerateCars;
        this.shouldGenerateBikes = shouldGenerateBikes;
        this.bikesNum = bikesNum;
        this.testBikeId = testBikeId;
        this.useFixedRoutes = useFixedRoutes;
        this.useFixedConstructionSites = useFixedConstructionSites;
        this.timeScale = timeScale;
        this.changeRouteOnTrafficJam = changeRouteOnTrafficJam;
        this.shouldGenerateTroublePoints = generateTroublePoints;
        this.timeBeforeTrouble = timeBeforeTrouble;
        this.pedestriansLimit = pedestriansLimit;
        this.testPedestrianId = testPedestrianId;
        this.startTime = startTime;
        this.lightStrategyActive = lightStrategyActive;
        this.extendLightTime = extendLightTime;
        this.stationStrategyActive = stationStrategyActive;
        this.extendWaitTime = extendWaitTime;
        this.changeRouteOnTroublePoint = changeRouteOnTroublePoint;
    }
}
