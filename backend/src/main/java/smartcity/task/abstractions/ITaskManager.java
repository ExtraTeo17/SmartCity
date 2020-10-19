package smartcity.task.abstractions;

import smartcity.lights.core.Light;

import java.util.Collection;
import java.util.function.BooleanSupplier;

public interface ITaskManager {
    void scheduleCarCreation(int carsLimit, int testCarId);

    void schedulePedestrianCreation(int pedestriansLimit, int testPedestrianId);

    void scheduleBusControl(BooleanSupplier testSimulationState);

    void scheduleSwitchLightTask(int managerId, Collection<Light> lights);

    void scheduleSimulationControl(BooleanSupplier testSimulationState, long nanoStartTime);

    void cancelAll();
}
