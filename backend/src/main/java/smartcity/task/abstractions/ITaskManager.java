package smartcity.task.abstractions;

import smartcity.lights.core.SimpleLightGroup;
import utilities.Siblings;

import java.time.LocalDateTime;
import java.util.function.BooleanSupplier;
//TODO:dokumentacja

public interface ITaskManager {
    void scheduleCarCreation(int carsLimit, int testCarId);

    void scheduleBikeCreation(int bikesLimit, int testBikeId);

    void schedulePedestrianCreation(int pedestriansLimit, int testPedestrianId);

    void scheduleBusControl(BooleanSupplier testSimulationState);

    void scheduleSwitchLightTask(int managerId, Siblings<SimpleLightGroup> lights);

    void scheduleSimulationControl(BooleanSupplier testSimulationState, LocalDateTime simulationStartTime);

    void cancelAll();
}
