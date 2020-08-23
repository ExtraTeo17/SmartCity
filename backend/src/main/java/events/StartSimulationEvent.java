package events;

public class StartSimulationEvent {
    public final int carsNum;
    public final int testCarId;

    public StartSimulationEvent(int carsNum, int testCarId) {
        this.carsNum = carsNum;
        this.testCarId = testCarId;
    }

}
