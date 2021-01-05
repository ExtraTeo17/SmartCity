package smartcity.task.abstractions;

import routing.abstractions.IRoutingHelper;
import routing.core.IGeoPosition;
import routing.nodes.StationNode;
import smartcity.lights.core.SimpleLightGroup;
import utilities.Siblings;

import java.time.LocalDateTime;
import java.util.function.Supplier;
//TODO:dokumentacja

public interface ITaskProvider {
    Runnable getCreateCarTask(IGeoPosition start, IGeoPosition end, boolean testCar);

    Runnable getCreateBikeTask(IGeoPosition start, IGeoPosition end, boolean testBike);

    Runnable getCreatePedestrianTask(IRoutingHelper routingHelper,
                                     StationNode startStation, StationNode endStation,
                                     boolean testPedestrian);

    Runnable getScheduleBusControlTask();

    Supplier<Integer> getSwitchLightsTask(int managerId, Siblings<SimpleLightGroup> lights);

    Runnable getSimulationControlTask(LocalDateTime simulationStartTime);
}
