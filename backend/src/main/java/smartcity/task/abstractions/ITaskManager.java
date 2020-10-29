package smartcity.task.abstractions;

import smartcity.lights.core.Light;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.function.BooleanSupplier;

public interface ITaskManager {
    void scheduleCarCreation(int carsLimit, int testCarId);

    void scheduleBikeCreation(int bikesLimit, int testbikeId);

    void schedulePedestrianCreation(int pedestriansLimit, int testPedestrianId);

    void scheduleBusControl(BooleanSupplier testSimulationState);

    void scheduleSwitchLightTask(int managerId, Collection<Light> lights);

    void scheduleSimulationControl(BooleanSupplier testSimulationState, LocalDateTime simulationStartTime);

    void cancelAll();
}
