package smartcity.task;

import agents.BikeAgent;
import agents.BusAgent;
import agents.CarAgent;
import agents.PedestrianAgent;
import agents.abstractions.IAgentsContainer;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.RoutingHelper;
import routing.abstractions.IRoutingHelper;
import routing.core.IZone;
import routing.core.Zone;
import smartcity.config.abstractions.IGenerationConfigContainer;
import smartcity.lights.core.SimpleLightGroup;
import smartcity.task.abstractions.ITaskManager;
import smartcity.task.abstractions.ITaskProvider;
import smartcity.task.runnable.abstractions.IRunnableFactory;
import utilities.Siblings;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static smartcity.task.GenerationConstants.*;

public class TaskManager implements ITaskManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private final IRunnableFactory runnableFactory;
    private final IAgentsContainer agentsContainer;
    private final IRoutingHelper routingHelper;
    private final ITaskProvider taskProvider;
    private final IGenerationConfigContainer configContainer;
    private final IZone zone;

    @Inject
    TaskManager(IRunnableFactory runnableFactory,
                IAgentsContainer agentsContainer,
                IRoutingHelper routingHelper,
                ITaskProvider taskProvider,
                IGenerationConfigContainer configContainer,
                IZone zone) {
        this.runnableFactory = runnableFactory;
        this.agentsContainer = agentsContainer;
        this.routingHelper = routingHelper;
        this.taskProvider = taskProvider;
        this.configContainer = configContainer;
        this.zone = zone;
    }

    @Override
    public void scheduleCarCreation(int carsLimit, int testCarId) {
        var adjustedZone = getAdjustedZone();
        var customRoutingHelper = getRoutingHelper(FIXED_CAR_SEED);
        Consumer<Integer> createCars = (runCount) -> {
            var randomPositions = customRoutingHelper.getRandomPositions(adjustedZone);
            taskProvider.getCreateCarTask(randomPositions.first, randomPositions.second, runCount == testCarId).run();
        };

        runIf(() -> agentsContainer.size(CarAgent.class) < carsLimit, createCars, CREATE_CAR_INTERVAL, true);
    }

    private IRoutingHelper getRoutingHelper(int seed) {
        return configContainer.shouldUseFixedRoutes() ?
                RoutingHelper.of(seed) :
                this.routingHelper;
    }

    @Override
    public void scheduleBikeCreation(int bikeLimit, int testBikeId) {
        var adjustedZone = getAdjustedZone();
        var customRoutingHelper = getRoutingHelper(FIXED_BIKE_SEED);
        Consumer<Integer> createBikes = (runCount) -> {
            var positions = customRoutingHelper.getRandomPositions(adjustedZone);
            taskProvider.getCreateBikeTask(positions.first, positions.second, runCount == testBikeId).run();
        };

        runIf(() -> agentsContainer.size(BikeAgent.class) < bikeLimit, createBikes, CREATE_BIKE_INTERVAL, true);
    }

    @Override
    public void schedulePedestrianCreation(int pedestriansLimit, int testPedestrianId) {
        IRoutingHelper customRoutingHelper;
        Random random;
        if (configContainer.shouldUseFixedRoutes()) {
            random = new Random(FIXED_PED_SEED);
            customRoutingHelper = RoutingHelper.of(random);
        }
        else {
            random = new Random();
            customRoutingHelper = this.routingHelper;
        }

        // Even in case of fixed routes there will be no race condition,
        //  because all tasks ale executed sequentially
        Consumer<Integer> createPedestrians = (runCount) -> {
            var busAgentOpt = getRandomBusAgent(random);
            if (busAgentOpt.isEmpty()) {
                logger.error("No buses exist");
                return;
            }

            var busAgent = busAgentOpt.get();
            var stationsOpt = busAgent.getTwoSubsequentStations(random);
            if (stationsOpt.isEmpty()) {
                logger.warn("Failed to get stations for pedestrian");
                return;
            }

            var stations = stationsOpt.get();
            taskProvider.getCreatePedestrianTask(customRoutingHelper, stations.first, stations.second,
                    runCount == testPedestrianId).run();
        };
        runIf(() -> agentsContainer.size(PedestrianAgent.class) < pedestriansLimit, createPedestrians,
                CREATE_PED_INTERVAL, true);
    }

    private IRoutingHelper getRoutingHelper(Random random) {
        return configContainer.shouldUseFixedRoutes() ?
                RoutingHelper.of(random) :
                this.routingHelper;
    }

    private Optional<BusAgent> getRandomBusAgent(Random random) {
        return agentsContainer.getRandom(BusAgent.class, random);
    }

    @Override
    public void scheduleBusControl(BooleanSupplier testSimulationState) {
        runWhile(testSimulationState, taskProvider.getScheduleBusControlTask(), BUS_CONTROL_INTERVAL);
    }

    @Override
    public void scheduleSwitchLightTask(int managerId, Siblings<SimpleLightGroup> lights) {
        var switchLightsTaskWithDelay = taskProvider.getSwitchLightsTask(managerId, lights);
        var runnable = runnableFactory.createDelay(switchLightsTaskWithDelay, false);
        runnable.runEndless(0, TIME_UNIT);
    }

    @Override
    public void scheduleSimulationControl(BooleanSupplier testSimulationState, LocalDateTime simulationStartTime) {
        var simulationControlTask = taskProvider.getSimulationControlTask(simulationStartTime);
        runWhile(testSimulationState, simulationControlTask, SIMULATION_CONTROL_INTERVAL);
    }

    @Override
    public void cancelAll() {
        var executors = runnableFactory.clearAllExecutors();
        for (var executor : executors) {
            try {
                executor.shutdownNow();
            } catch (Exception e) {
                logger.warn("Error cancelling executor: ", e);
            }
        }
    }

    private IZone getAdjustedZone() {
        var adjustedRadius = this.zone.getRadius() - ZONE_ADJUSTMENT;
        return Zone.of(this.zone.getCenter(), adjustedRadius);
    }

    private void runNTimes(Consumer<Integer> runCountConsumer, int runCount, int interval) {
        runNTimes(runCountConsumer, runCount, interval, false);
    }

    private void runNTimes(Consumer<Integer> runCountConsumer, int runCount, int interval, boolean separateThread) {
        var runnable = runnableFactory.createCount(runCountConsumer, runCount, separateThread);
        runnable.runFixed(interval, TIME_UNIT);
    }

    private void runWhile(BooleanSupplier test, Runnable action, int interval) {
        var runnable = runnableFactory.createWhile(test, action);
        runnable.runFixed(interval, TIME_UNIT);
    }

    private void runIf(BooleanSupplier test, Runnable action, int interval, boolean separateThread) {
        var runnable = runnableFactory.createIf(test, action, separateThread);
        runnable.runFixed(interval, TIME_UNIT);
    }

    private void runIf(BooleanSupplier test, Consumer<Integer> action, int interval, boolean separateThread) {
        var runnable = runnableFactory.createIf(test, action, separateThread);
        runnable.runFixed(interval, TIME_UNIT);
    }
}
