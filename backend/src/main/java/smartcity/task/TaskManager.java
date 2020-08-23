package smartcity.task;

import agents.BusAgent;
import agents.LightManager;
import agents.VehicleAgent;
import agents.abstractions.AbstractAgent;
import agents.abstractions.IAgentsContainer;
import agents.abstractions.IAgentsFactory;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.StartSimulationEvent;
import events.VehicleAgentCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.RouteNode;
import routing.Router;
import routing.StationNode;
import routing.core.IGeoPosition;
import routing.core.IZone;
import routing.core.Position;
import smartcity.SimulationState;
import smartcity.config.ConfigContainer;
import smartcity.task.runnable.IRunnableFactory;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TaskManager implements ITaskManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
    private static final int CREATE_CAR_INTERVAL = 500;
    private static final int CREATE_PEDESTRIAN_INTERVAL = 100;
    private static final int BUS_CONTROL_INTERVAL = 2000;

    private final IRunnableFactory runnableFactory;
    private final IAgentsFactory agentsFactory;
    private final IAgentsContainer agentsContainer;
    private final EventBus eventBus;
    private final IZone zone;
    private final ConfigContainer configContainer;

    private final Random random;
    private final Table<IGeoPosition, IGeoPosition, List<RouteNode>> routeInfoCache;

    @Inject
    TaskManager(IRunnableFactory runnableFactory,
                IAgentsFactory agentsFactory,
                IAgentsContainer agentsContainer,
                EventBus eventBus,
                ConfigContainer configContainer) {
        this.runnableFactory = runnableFactory;
        this.agentsFactory = agentsFactory;
        this.agentsContainer = agentsContainer;
        this.eventBus = eventBus;
        this.zone = configContainer.getZone();
        this.configContainer =configContainer;

        this.random = new Random();
        this.routeInfoCache = HashBasedTable.create();
    }

    @SuppressWarnings("FeatureEnvy")
    @Subscribe
    public void handle(StartSimulationEvent e) {
        activateLightManagerAgents();
        if (configContainer.shouldGenerateCars()) {
            scheduleCarCreation(e.carsNum, e.testCarId);
        }
        if (configContainer.shouldGeneratePedestriansAndBuses()) {
            // TODO: Add pedestrians limit and testPedestrianID
            schedulePedestrianCreation(100_000, e.testCarId);
            scheduleBusControl(simulationState -> simulationState == SimulationState.RUNNING,
                    configContainer::getSimulationState);
        }

        configContainer.setSimulationState(SimulationState.RUNNING);
    }

    private void activateLightManagerAgents() {
        agentsContainer.forEach(LightManager.class, AbstractAgent::start);
    }

    @Override
    public void scheduleCarCreation(int numberOfCars, int testCarId) {
        Consumer<Integer> createCars = (Integer counter) -> {
            var zoneCenter = zone.getCenter();
            var geoPosInZoneCircle = generateRandomOffset(zone.getRadius());
            var posA = zoneCenter.sum(geoPosInZoneCircle);
            var posB = zoneCenter.diff(geoPosInZoneCircle);

            getCreateCarTask(posA, posB, counter == testCarId).run();
        };

        runNTimes(createCars, numberOfCars, CREATE_CAR_INTERVAL);
    }

    public Runnable getCreateCarTask(IGeoPosition start, IGeoPosition end, boolean testCar) {
        return () -> {
            List<RouteNode> info;
            try {
                info = routeInfoCache.get(start, end);
                if (info == null) {
                    info = Router.generateRouteInfo(start, end);
                    routeInfoCache.put(start, end, info);
                }
            } catch (Exception e) {
                logger.warn("Error generating route info", e);
                return;
            }

            VehicleAgent agent = agentsFactory.create(info, testCar);
            if (agentsContainer.tryAdd(agent)) {
                agent.start();
                eventBus.post(new VehicleAgentCreatedEvent(agent.getPosition()));
            }
        };
    }

    private IGeoPosition generateRandomOffset(int radius) {
        double angle = random.nextDouble() * Math.PI * 2;
        double lat = Math.sin(angle) * radius * Router.DEGREES_PER_METER;
        double lng = Math.cos(angle) * radius * Router.DEGREES_PER_METER * Math.cos(lat);
        return Position.of(lat, lng);
    }

    @Override
    public void schedulePedestrianCreation(int numberOfPedestrians, int testPedestrianId) {
        Consumer<Integer> createCars = (Integer counter) -> {
            var busAgentOpt = getRandomBusAgent();
            if (busAgentOpt.isEmpty()) {
                logger.error("No buses exist");
                return;
            }
            var busAgent = busAgentOpt.get();
            var stations = busAgent.getTwoSubsequentStations(random);
            // TODO: Move more logic here

            getCreatePedestrianTask(stations.first, stations.second, busAgent.getLine(),
                    counter == testPedestrianId).run();
        };
        runNTimes(createCars, numberOfPedestrians, CREATE_PEDESTRIAN_INTERVAL, true);
    }

    @Override
    public Runnable getCreatePedestrianTask(StationNode startStation, StationNode endStation,
                                            String busLine, boolean testPedestrian) {
        return () -> {
            try {
                // TODO: Generating this offset doesn't work!
                var geoPosInFirstStationCircle = generateRandomOffset(200);
                IGeoPosition pedestrianStartPoint = startStation.sum(geoPosInFirstStationCircle);
                IGeoPosition pedestrianFinishPoint = endStation.diff(geoPosInFirstStationCircle);

                // TODO: No null here
                List<RouteNode> routeToStation = Router.generateRouteInfoForPedestrians(pedestrianStartPoint, startStation,
                        null, startStation.getOsmStationId());
                List<RouteNode> routeFromStation = Router.generateRouteInfoForPedestrians(endStation, pedestrianFinishPoint,
                        endStation.getOsmStationId(), null);

                // TODO: Separate fields for testPedestrian and pedestriansLimit
                var agent = agentsFactory.create(routeToStation, routeFromStation,
                        busLine, startStation, endStation, testPedestrian);
                agent.start();
            } catch (Exception e) {
                logger.warn("Unknown error in pedestrian creation", e);
            }
        };
    }

    private Optional<BusAgent> getRandomBusAgent() {
        return agentsContainer.getRandom(BusAgent.class, random);
    }

    @Override
    public void scheduleBusControl(Predicate<SimulationState> testSimulationState, Supplier<SimulationState> getSimulationState) {
        runWhile(testSimulationState, getSimulationState, getScheduleBusControlTask(), BUS_CONTROL_INTERVAL);
    }

    @Override
    public Runnable getScheduleBusControlTask() {
        return () -> {
            try {
                agentsContainer.forEach(BusAgent.class, BusAgent::runBasedOnTimetable);
            } catch (Exception e) {
                logger.warn("Error in bus control task", e);
            }
        };
    }

    private void runNTimes(Consumer<Integer> runCountConsumer, int runCount, int interval, boolean separateThread) {
        var runnable = runnableFactory.create(runCountConsumer, runCount, separateThread);
        runnable.runFixed(interval, TIME_UNIT);
    }

    private void runNTimes(Consumer<Integer> runCountConsumer, int runCount, int interval) {
        runNTimes(runCountConsumer, runCount, interval, false);
    }

    private <T> void runWhile(Predicate<T> predicate, Supplier<T> supplier, Runnable action, int interval) {
        var runnable = runnableFactory.create(predicate, supplier, action);
        runnable.runFixed(interval, TIME_UNIT);
    }
}
