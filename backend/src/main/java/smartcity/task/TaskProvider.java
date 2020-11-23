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
import events.web.bike.BikeAgentCreatedEvent;
import events.web.bus.BusAgentStartedEvent;
import events.web.car.CarAgentCreatedEvent;
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
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("OverlyCoupledClass")
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

    private final Table<IGeoPosition, IGeoPosition, List<RouteNode>> routeInfoCache;

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

        this.routeInfoCache = HashBasedTable.create();
    }

    public Runnable getCreateCarTask(IGeoPosition start, IGeoPosition end, boolean testCar) {
        return () -> {
            List<RouteNode> route;
            try {
                route = routeInfoCache.get(start, end);
                if (route == null) {
                    route = routeGenerator.generateRouteInfo(start, end, false);
                    routeInfoCache.put(start, end, route);
                }
            } catch (Exception e) {
                logger.warn("Error generating route info", e);
                return;
            }

            CarAgent agent = agentsFactory.create(route, testCar);
            if (agentsContainer.tryAdd(agent)) {
                agent.start();
                eventBus.post(new CarAgentCreatedEvent(agent.getId(), agent.getPosition(), route, testCar));
            }
        };
    }


    public Runnable getCreateBikeTask(IGeoPosition start, IGeoPosition end, boolean testBike) {
        return () -> {
            List<RouteNode> route;
            try {
                route = routeInfoCache.get(start, end);
                if (route == null) {
                    route = routeGenerator.generateRouteInfo(start, end, "bike");
                    // TODO: If car start & end will be equal to bike, then car can receive bike route.
                    routeInfoCache.put(start, end, route);
                }
            } catch (Exception e) {
                logger.warn("Error generating route info", e);
                return;
            }

            BikeAgent agent = agentsFactory.create(route, testBike, "");
            if (agentsContainer.tryAdd(agent)) {
                agent.start();
                logger.info("Started: " + agent.getLocalName());
                eventBus.post(new BikeAgentCreatedEvent(agent.getId(), agent.getPosition(), route, testBike));
            }
        };
    }

    @Override
    public Runnable getCreatePedestrianTask(IRoutingHelper routingHelper,
                                            StationNode startStation, StationNode endStation,
                                            String busLine, boolean testPedestrian) {
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

                PedestrianAgent agent = agentsFactory.create(routeToStation, routeFromStation,
                        busLine, startStation, endStation, testPedestrian);
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

    @Override
    public Supplier<Integer> getSwitchLightsTask(int managerId, Siblings<SimpleLightGroup> lights) {
        var switchLights = functionalTaskFactory
                .createLightSwitcher(managerId, configContainer.getExtendLightTime(), lights);
        // Can be moved somewhere else if needed and passed as parameter
        var switchLightsContext = new ISwitchLightsContext() {
            private boolean haveAlreadyExtended = false;

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

    @Override
    public Runnable getSimulationControlTask(LocalDateTime simulationStartTime) {
        // TODO: Batch update of cars positions: eventBus.post();
        return timeProvider.getUpdateTimeTask(simulationStartTime);
    }
}
