package events.web;

import java.time.LocalDateTime;

public class StartSimulationEvent {
    public final boolean shouldGenerateCars;
    public final int carsNum;
    public final int testCarId;
    public final boolean shouldGenerateTroublePoints;

    public final int pedestriansLimit;
    public final int testPedestrianId;

    // TODO: Maybe include post time to reduce discrepancy between frontend and backend
    public final LocalDateTime startTime;

    public final boolean lightStrategyActive;
    public final int extendLightTime;

    public final boolean stationStrategyActive;
    public final int extendWaitTime;

    public final boolean changeRouteStrategyActive;

    public StartSimulationEvent(int carsNum, int testCarId,
                                boolean shouldGenerateCars, boolean generateTroublePoints,
                                int pedestriansLimit, int testPedestrianId,
                                LocalDateTime startTime, boolean lightStrategyActive,
                                int extendLightTime, boolean stationStrategyActive,
                                int extendWaitTime, boolean changeRouteStrategyActive) {
        this.carsNum = carsNum;
        this.testCarId = testCarId;
        this.shouldGenerateCars = shouldGenerateCars;
        this.shouldGenerateTroublePoints = generateTroublePoints;
        this.pedestriansLimit = pedestriansLimit;
        this.testPedestrianId = testPedestrianId;
        this.startTime = startTime;
        this.lightStrategyActive = lightStrategyActive;
        this.extendLightTime = extendLightTime;
        this.stationStrategyActive = stationStrategyActive;
        this.extendWaitTime = extendWaitTime;
        this.changeRouteStrategyActive = changeRouteStrategyActive;
    }
}
