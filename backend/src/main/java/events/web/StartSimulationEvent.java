package events.web;

public class StartSimulationEvent {
    public final int carsNum;
    public final int testCarId;
    public final boolean shouldGenerateCars;
    public final boolean shouldGenerateTroublePoints;

    public StartSimulationEvent(int carsNum, int testCarId,
                                boolean shouldGenerateCars, boolean generateTroublePoints) {
        this.carsNum = carsNum;
        this.testCarId = testCarId;
        this.shouldGenerateCars = shouldGenerateCars;
        this.shouldGenerateTroublePoints = generateTroublePoints;
    }

}
