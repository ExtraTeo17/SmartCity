package smartcity.task;

import agents.BikeAgent;
import agents.BusAgent;
import agents.CarAgent;
import agents.PedestrianAgent;
import agents.abstractions.IAgentsContainer;
import agents.abstractions.IAgentsFactory;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import events.web.BatchedUpdateEvent;
import events.web.bike.BikeAgentCreatedEvent;
import events.web.bus.BusAgentStartedEvent;
import events.web.car.CarAgentCreatedEvent;
import events.web.models.UpdateObject;
import events.web.pedestrian.PedestrianAgentCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.abstractions.IRouteGenerator;
import routing.abstractions.IRoutingHelper;
import routing.core.IGeoPosition;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;
import smartcity.ITimeProvider;
import smartcity.config.ConfigContainer;
import smartcity.lights.core.SimpleLightGroup;
import smartcity.task.abstractions.ITaskProvider;
import smartcity.task.data.ISwitchLightsContext;
import smartcity.task.functional.IFunctionalTaskFactory;
import utilities.Siblings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static smartcity.config.StaticConfig.USE_BATCHED_UPDATES;


/**
 * Used for periodical tasks creation. <br/>
 * Note that this entity only creates the tasks, not run them. You can treat this class as logic encapsulation mechanism. <br/>
 * Each method has complexity of O(1).
 */
@SuppressWarnings({"OverlyCoupledClass", "ClassWithTooManyFields"})
public class TaskProvider implements ITaskProvider {
    private static final Logger logger = LoggerFactory.getLogger(TaskProvider.class);
    private final int pedestrianRouteOffset = 200;

    private final ConfigContainer configContainer;
    private final IRouteGenerator routeGenerator;
    private final IAgentsFactory agentsFactory;
    private final IAgentsContainer agentsContainer;
    private final IFunctionalTaskFactory functionalTaskFactory;
    private final ITimeProvider timeProvider;
    private final EventBus eventBus;

    private final Table<IGeoPosition, IGeoPosition, List<RouteNode>> carRouteInfoCache;
    private final Table<IGeoPosition, IGeoPosition, List<RouteNode>> bikeRouteInfoCache;

    private IGeoPosition startForBatches;
    private IGeoPosition endForBatches;

    @Inject
    public TaskProvider(ConfigContainer configContainer,
                        IRouteGenerator routeGenerator,
                        IAgentsFactory agentsFactory,
                        IAgentsContainer agentsContainer,
                        IFunctionalTaskFactory functionalTaskFactory,
                        ITimeProvider timeProvider,
                        EventBus eventBus) {
        this.configContainer = configContainer;
        this.routeGenerator = routeGenerator;
        this.agentsFactory = agentsFactory;
        this.agentsContainer = agentsContainer;
        this.functionalTaskFactory = functionalTaskFactory;
        this.timeProvider = timeProvider;
        this.eventBus = eventBus;

        this.carRouteInfoCache = HashBasedTable.create();
        this.bikeRouteInfoCache = HashBasedTable.create();
    }

    /**
     * Used to create task, which creates car agent and adds him to the current {@link IAgentsContainer}
     *
     * @param start   Start position
     * @param end     End position
     * @param testCar If should be test car.
     * @return -
     */
    @Override
    public Runnable getCreateCarTask(IGeoPosition start, IGeoPosition end, boolean testCar) {
        return () -> {
            List<RouteNode> route = tryRetrieveCarRoute(start, end);

            if (route.size() == 0) {
                logger.warn("Generated route is empty, agent won't be created.");
                startForBatches = endForBatches = null;
                return;
            }

            CarAgent agent = agentsFactory.create(route, testCar);
            if (agentsContainer.tryAdd(agent)) {
                agent.start();
                eventBus.post(new CarAgentCreatedEvent(agent.getId(), agent.getPosition(), route, testCar));
            }
        };
    }

    private List<RouteNode> tryRetrieveCarRoute(IGeoPosition start, IGeoPosition end) {
        List<RouteNode> route;
        try {
            var effectiveStart = start;
            var effectiveEnd = end;
            if (configContainer.shouldGenerateBatchesForCars()) {
                if (startForBatches == null && endForBatches == null) {
                    startForBatches = start;
                    endForBatches = end;
                }

                effectiveStart = startForBatches;
                effectiveEnd = endForBatches;
            }

            route = carRouteInfoCache.get(effectiveStart, effectiveEnd);
            if (route == null) {
                route = routeGenerator.generateRouteInfo(effectiveStart, effectiveEnd, false);
                carRouteInfoCache.put(effectiveStart, effectiveEnd, route);
            }
            else {
                logger.info("Successfully retrieved car route from cache");
            }
        } catch (Exception e) {
            logger.warn("Error generating route info", e);
            return new ArrayList<>();
        }

        return route;
    }

    /**
     * Used to create task, which creates bike agent and adds him to the current {@link IAgentsContainer}
     *
     * @param start    Start position
     * @param end      End position
     * @param testBike If should be test bike
     * @return -
     */
    @Override
    public Runnable getCreateBikeTask(IGeoPosition start, IGeoPosition end, boolean testBike) {
        return () -> {
            List<RouteNode> route = tryRetrieveBikeRoute(start, end);

            if (route.size() == 0) {
                logger.debug("Generated route is empty, agent won't be created.");
                return;
            }

            BikeAgent agent = agentsFactory.createBike(route, testBike);
            if (agentsContainer.tryAdd(agent)) {
                agent.start();
                logger.info("Started: " + agent.getLocalName());
                eventBus.post(new BikeAgentCreatedEvent(agent.getId(), agent.getPosition(), route, testBike));
            }
        };
    }

    private List<RouteNode> tryRetrieveBikeRoute(IGeoPosition start, IGeoPosition end) {
        List<RouteNode> route;
        try {
            route = bikeRouteInfoCache.get(start, end);
            if (route == null) {
                route = routeGenerator.generateRouteInfo(start, end, "bike");
                bikeRouteInfoCache.put(start, end, route);
            }
            else {
                logger.info("Successfully retrieved bike route from cache");
            }
        } catch (Exception e) {
            logger.warn("Error generating route info", e);
            return new ArrayList<>();
        }

        return route;
    }

    /**
     * Used to create task, which creates pedestrian agent and adds him to the current {@link IAgentsContainer}
     *
     * @param routingHelper  Used for start/end positions generation
     * @param startStation   Station, which pedestrian will initially head to, to wait for bus.
     * @param endStation     Station, at which pedestrian get off from bus.
     * @param testPedestrian If should be test pedestrian.
     * @return -
     */
    @Override
    public Runnable getCreatePedestrianTask(IRoutingHelper routingHelper,
                                            StationNode startStation, StationNode endStation,
                                            boolean testPedestrian) {
        return () -> {
            try {
                var randomOffsetStart = routingHelper.generateRandomOffset(pedestrianRouteOffset, startStation.getLat());
                var pedestrianStartPoint = startStation.sum(randomOffsetStart);

                var randomOffsetEnd = routingHelper.generateRandomOffset(pedestrianRouteOffset, endStation.getLat());
                var pedestrianFinishPoint = endStation.diff(randomOffsetEnd);

                // TODO: No null here
                List<RouteNode> routeToStation = routeGenerator.generateRouteForPedestrians(
                        pedestrianStartPoint,
                        startStation,
                        null,
                        String.valueOf(startStation.getOsmId()));
                List<RouteNode> routeFromStation = routeGenerator.generateRouteForPedestrians(
                        endStation,
                        pedestrianFinishPoint,
                        String.valueOf(endStation.getOsmId()),
                        null);
                if (routeToStation.size() == 0 || routeFromStation.size() == 0) {
                    logger.debug("Generated route is empty, agent won't be created.");
                    return;
                }

                PedestrianAgent agent = agentsFactory.create(routeToStation, routeFromStation,
                        startStation, endStation, testPedestrian);
                if (agentsContainer.tryAdd(agent)) {
                    agent.start();
                    eventBus.post(new PedestrianAgentCreatedEvent(agent.getId(), agent.getPosition(),
                            routeToStation,
                            routeFromStation,
                            testPedestrian));
                }
            } catch (Exception e) {
                logger.warn("Unknown error in pedestrian creation", e);
            }
        };
    }

    /**
     * Used to create task for starting bus agents on schedule.
     *
     * @return -
     */
    @SuppressWarnings("FeatureEnvy")
    @Override
    public Runnable getScheduleBusControlTask() {
        return () -> {
            try {
                agentsContainer.forEach(BusAgent.class, (busAgent) -> {
                    // Agent was created but not accepted.
                    if (busAgent.shouldStart()) {
                        eventBus.post(new BusAgentStartedEvent(busAgent.getId()));
                        if (busAgent.getAID() == null) {
                            agentsContainer.tryAccept(busAgent);
                        }
                    }
                    busAgent.runBasedOnTimetable();
                });
            } catch (Exception e) {
                logger.warn("Error in bus control task", e);
            }
        };
    }

    /**
     * Used to create task for light-group light-color updates.
     *
     * @param managerId Corresponding {@link agents.LightManagerAgent} id
     * @param lights    All lights that should simultaneously be switched
     * @return Task, which returns period after it (the Task) should be executed again.
     */
    @Override
    public Supplier<Integer> getSwitchLightsTask(int managerId, Siblings<SimpleLightGroup> lights) {
        var switchLights = functionalTaskFactory
                .createLightSwitcher(managerId, configContainer.getExtendLightTime(), lights);
        // Can be moved somewhere else if needed and passed as parameter
        var switchLightsContext = new ISwitchLightsContext() {
            private boolean haveAlreadyExtended;

            @Override
            public boolean haveAlreadyExtended() {
                return haveAlreadyExtended;
            }

            @Override
            public void setAlreadyExtendedGreen(boolean value) {
                haveAlreadyExtended = value;
            }
        };

        return () -> switchLights.apply(switchLightsContext);
    }

    /**
     * Used to create task for simulation time updates. <br/>
     * If {@link smartcity.config.StaticConfig#USE_BATCHED_UPDATES} is true, then this method will return {@link Runnable},
     * that will collect all agent positions from current {@link IAgentsContainer} and post event containing this data. <br/>
     * Posted event is later used to update agent positions on GUI. <br/>
     * It is important to notice that positions will be collected regardless whether position of agent changed or not.
     *
     * @param simulationStartTime Initial simulation time
     * @return Task, which should be executed periodically to update simulation
     */
    @Override
    public Runnable getSimulationControlTask(LocalDateTime simulationStartTime) {
        var updateTimeTask = timeProvider.getUpdateTimeTask(simulationStartTime);
        return () -> {
            if (USE_BATCHED_UPDATES) {
                var carUpdates = new ArrayList<UpdateObject>(agentsContainer.size(CarAgent.class));
                agentsContainer.forEach(CarAgent.class, c -> {
                    carUpdates.add(new UpdateObject(c.getId(), c.getPosition()));
                });

                var bikeUpdates = new ArrayList<UpdateObject>(agentsContainer.size(BikeAgent.class));
                agentsContainer.forEach(BikeAgent.class, b -> {
                    bikeUpdates.add(new UpdateObject(b.getId(), b.getPosition()));
                });

                var busUpdates = new ArrayList<UpdateObject>();
                agentsContainer.forEach(BusAgent.class, b -> {
                    if (b.isAlive()) {
                        busUpdates.add(new UpdateObject(b.getId(), b.getPosition()));
                    }
                });

                var pedUpdates = new ArrayList<UpdateObject>(agentsContainer.size(PedestrianAgent.class));
                agentsContainer.forEach(PedestrianAgent.class, p -> {
                    pedUpdates.add(new UpdateObject(p.getId(), p.getPosition()));
                });

                eventBus.post(new BatchedUpdateEvent(carUpdates, bikeUpdates, busUpdates, pedUpdates));
            }
            updateTimeTask.run();
        };
    }
}
