package smartcity.task;

import agents.BusAgent;
import agents.abstractions.IAgentsContainer;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.abstractions.IRoutingHelper;
import routing.core.IZone;
import smartcity.lights.core.Light;
import smartcity.task.abstractions.ITaskManager;
import smartcity.task.abstractions.ITaskProvider;
import smartcity.task.runnable.abstractions.IRunnableFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class TaskManager implements ITaskManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
    private static final int CREATE_CAR_INTERVAL = 500;
    private static final int CREATE_PEDESTRIAN_INTERVAL = 120;
    private static final int BUS_CONTROL_INTERVAL = 2000;

    private final IRunnableFactory runnableFactory;
    private final IAgentsContainer agentsContainer;
    private final IRoutingHelper routingHelper;
    private final ITaskProvider taskProvider;
    private final IZone zone;

    private final Random random;

    @Inject
    TaskManager(IRunnableFactory runnableFactory,
                IAgentsContainer agentsContainer,
                IRoutingHelper routingHelper,
                ITaskProvider taskProvider,
                IZone zone) {
        this.runnableFactory = runnableFactory;
        this.agentsContainer = agentsContainer;
        this.routingHelper = routingHelper;
        this.taskProvider = taskProvider;
        this.zone = zone;

        this.random = new Random();
    }

    @Override
    public void scheduleCarCreation(int numberOfCars, int testCarId) {
        Consumer<Integer> createCars = (Integer counter) -> {
            var zoneCenter = zone.getCenter();
            var geoPosInZoneCircle = routingHelper.generateRandomOffset(zone.getRadius());
            var posA = zoneCenter.sum(geoPosInZoneCircle);
            var posB = zoneCenter.diff(geoPosInZoneCircle);

            taskProvider.getCreateCarTask(posA, posB, counter == testCarId).run();
        };

        runNTimes(createCars, numberOfCars, CREATE_CAR_INTERVAL);
    }

    @Override
    public void schedulePedestrianCreation(int numberOfPedestrians, int testPedestrianId) {
        Consumer<Integer> createPedestrians = (Integer counter) -> {
            var busAgentOpt = getRandomBusAgent();
            if (busAgentOpt.isEmpty()) {
                logger.error("No buses exist");
                return;
            }
            var busAgent = busAgentOpt.get();
            var stations = busAgent.getTwoSubsequentStations(random);

            // TODO: Move more logic here
            taskProvider.getCreatePedestrianTask(stations.first, stations.second, busAgent.getLine(),
                    counter == testPedestrianId).run();
        };
        runNTimes(createPedestrians, numberOfPedestrians, CREATE_PEDESTRIAN_INTERVAL, true);
    }

    private Optional<BusAgent> getRandomBusAgent() {
        return agentsContainer.getRandom(BusAgent.class, random);
    }

    @Override
    public void scheduleBusControl(BooleanSupplier testSimulationState) {
        runWhile(testSimulationState, taskProvider.getScheduleBusControlTask(), BUS_CONTROL_INTERVAL);
    }

    @Override
    public void scheduleSwitchLightTask(Collection<Light> lights) {
        var switchLightsTaskWithDelay = taskProvider.getSwitchLightsTask(lights);
        var runnable = runnableFactory.create(switchLightsTaskWithDelay);
        runnable.runEndless(0, TIME_UNIT);
    }


    private void runNTimes(Consumer<Integer> runCountConsumer, int runCount, int interval) {
        runNTimes(runCountConsumer, runCount, interval, false);
    }

    private void runNTimes(Consumer<Integer> runCountConsumer, int runCount, int interval, boolean separateThread) {
        var runnable = runnableFactory.create(runCountConsumer, runCount, separateThread);
        runnable.runFixed(interval, TIME_UNIT);
    }

    private void runWhile(BooleanSupplier test, Runnable action, int interval) {
        var runnable = runnableFactory.create(test, action);
        runnable.runFixed(interval, TIME_UNIT);
    }
}
