package events.web;

import java.time.LocalDateTime;

public class StartSimulationEvent {
    public final int carsNum;
    public final int testCarId;
    public final boolean shouldGenerateCars;
    public final boolean shouldGenerateTroublePoints;
    // TODO: Maybe include post time to reduce discrepancy between frontend and backend
    public final LocalDateTime startTime;

    public StartSimulationEvent(int carsNum, int testCarId,
                                boolean shouldGenerateCars, boolean generateTroublePoints,
                                LocalDateTime startTime) {
        this.carsNum = carsNum;
        this.testCarId = testCarId;
        this.shouldGenerateCars = shouldGenerateCars;
        this.shouldGenerateTroublePoints = generateTroublePoints;
        this.startTime = startTime;
    }
}
