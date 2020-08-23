package smartcity.task;

import routing.StationNode;
import routing.core.IGeoPosition;
import smartcity.SimulationState;

import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ITaskManager {
    void scheduleCarCreation(int numberOfCars, int testCarId);

    Runnable getCreateCarTask(IGeoPosition start, IGeoPosition end, boolean testCar);

    void schedulePedestrianCreation(int numberOfPedestrians, int testPedestrianId);

    Runnable getCreatePedestrianTask(StationNode startStation, StationNode endStation,
                                     String busLine, boolean testPedestrian);

    void scheduleBusControl(Predicate<SimulationState> testSimulationState, Supplier<SimulationState> getSimulationState);

    public Runnable getScheduleBusControlTask();
}
