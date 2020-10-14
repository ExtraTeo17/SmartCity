package smartcity.task.abstractions;

import routing.nodes.StationNode;
import routing.core.IGeoPosition;
import smartcity.lights.core.Light;

import java.util.Collection;
import java.util.function.Supplier;

public interface ITaskProvider {
    Runnable getCreateCarTask(IGeoPosition start, IGeoPosition end, boolean testCar);

    Runnable getCreatePedestrianTask(StationNode startStation, StationNode endStation,
                                     String busLine, boolean testPedestrian);

    Runnable getScheduleBusControlTask();

    Supplier<Integer> getSwitchLightsTask(int managerId, Collection<Light> lights);
}
