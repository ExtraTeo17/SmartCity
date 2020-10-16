package smartcity.task;

import agents.BusAgent;
import agents.PedestrianAgent;
import agents.VehicleAgent;
import agents.abstractions.IAgentsContainer;
import agents.abstractions.IAgentsFactory;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import events.web.VehicleAgentCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.abstractions.IRouteGenerator;
import routing.abstractions.IRoutingHelper;
import routing.core.IGeoPosition;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;
import smartcity.ITimeProvider;
import smartcity.TimeProvider;
import smartcity.config.ConfigContainer;
import smartcity.lights.core.Light;
import smartcity.task.abstractions.ITaskProvider;
import smartcity.task.data.ISwitchLightsContext;
import smartcity.task.functional.IFunctionalTaskFactory;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class TaskProvider implements ITaskProvider {
    private static final Logger logger = LoggerFactory.getLogger(TaskProvider.class);

    private final ConfigContainer configContainer;
    private final IRouteGenerator routeGenerator;
    private final IRoutingHelper routingHelper;
    private final IAgentsFactory agentsFactory;
    private final IAgentsContainer agentsContainer;
    private final IFunctionalTaskFactory functionalTaskFactory;
    private final ITimeProvider timeProvider;
    private final EventBus eventBus;

    private final Table<IGeoPosition, IGeoPosition, List<RouteNode>> routeInfoCache;

    @Inject
    public TaskProvider(ConfigContainer configContainer, IRouteGenerator routeGenerator,
                        IRoutingHelper routingHelper,
                        IAgentsFactory agentsFactory,
                        IAgentsContainer agentsContainer,
                        IFunctionalTaskFactory functionalTaskFactory,
                        ITimeProvider timeProvider,
                        EventBus eventBus) {
        this.configContainer = configContainer;
        this.routeGenerator = routeGenerator;
        this.routingHelper = routingHelper;
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
                    route = routeGenerator.generateRouteInfo(start, end);
                    routeInfoCache.put(start, end, route);
                }
            } catch (Exception e) {
                logger.warn("Error generating route info", e);
                return;
            }

            VehicleAgent agent = agentsFactory.create(route, testCar);
            if (agentsContainer.tryAdd(agent)) {
                agent.start();
                eventBus.post(new VehicleAgentCreatedEvent(agent.getId(), agent.getPosition(), route, testCar));
            }
        };
    }

    @Override
    public Runnable getCreatePedestrianTask(StationNode startStation, StationNode endStation,
                                            String busLine, boolean testPedestrian) {
        return () -> {
            try {
                // TODO: Generating this offset doesn't work?
                var geoPosInFirstStationCircle = routingHelper.generateRandomOffset(200);
                IGeoPosition pedestrianStartPoint = startStation.sum(geoPosInFirstStationCircle);
                IGeoPosition pedestrianFinishPoint = endStation.diff(geoPosInFirstStationCircle);

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

                // TODO: Separate fields for testPedestrian and pedestriansLimit
                PedestrianAgent agent = agentsFactory.create(routeToStation, routeFromStation,
                        busLine, startStation, endStation, testPedestrian);
                if (agentsContainer.tryAdd(agent)) {
                    agent.start();
                }
            } catch (Exception e) {
                logger.warn("Unknown error in pedestrian creation", e);
            }
        };
    }

    @Override
    public Runnable getScheduleBusControlTask() {
        return () -> {
            try {
                agentsContainer.removeIf(BusAgent.class, BusAgent::runBasedOnTimetable);
            } catch (Exception e) {
                logger.warn("Error in bus control task", e);
            }
        };
    }

    @Override
    public Supplier<Integer> getSwitchLightsTask(int managerId, Collection<Light> lights) {
        var switchLights = functionalTaskFactory
                .createLightSwitcher(managerId, configContainer.getExtendTimeSeconds(), lights);
        // Can be moved somewhere else if needed and passed as parameter
        var switchLightsContext = new ISwitchLightsContext() {
            private boolean alreadyExtendedGreen = false;

            @Override
            public boolean haveAlreadyExtendedGreen() {
                return alreadyExtendedGreen;
            }

            @Override
            public void setExtendedGreen(boolean value) {
                alreadyExtendedGreen = value;
            }
        };

        return () -> switchLights.apply(switchLightsContext);
    }

    @Override
    public Runnable getSimulationControlTask(long nanoStartTime) {
        var updateTimeTask = timeProvider.getUpdateTimeTask((System.nanoTime() - nanoStartTime) /
                (1_000_000 * TimeProvider.MS_PER_TICK));
        return () -> {
            updateTimeTask.run();
            // TODO: Batch update of cars positions: eventBus.post();
        };
    }
}
