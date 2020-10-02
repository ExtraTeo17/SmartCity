package smartcity.task.abstractions;

import smartcity.lights.core.Light;

import java.util.Collection;
import java.util.function.BooleanSupplier;

public interface ITaskManager {
    void scheduleCarCreation(int numberOfCars, int testCarId);

    void schedulePedestrianCreation(int numberOfPedestrians, int testPedestrianId);

    void scheduleBusControl(BooleanSupplier testSimulationState);

    void scheduleSwitchLightTask(Collection<Light> lights);
}
