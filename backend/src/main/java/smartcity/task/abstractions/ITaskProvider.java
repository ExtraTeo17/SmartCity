package smartcity.task.abstractions;

import routing.core.IGeoPosition;
import routing.nodes.StationNode;
import smartcity.lights.core.Light;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.function.Supplier;

public interface ITaskProvider {
    Runnable getCreateCarTask(IGeoPosition start, IGeoPosition end, boolean testCar);

    Runnable getCreateBikeTask(IGeoPosition start, IGeoPosition end, boolean testBike);


    Runnable getCreatePedestrianTask(StationNode startStation, StationNode endStation,
                                     boolean testPedestrian);

    Runnable getScheduleBusControlTask();

    Supplier<Integer> getSwitchLightsTask(int managerId, Collection<Light> lights);

    Runnable getSimulationControlTask(LocalDateTime simulationStartTime);
}
