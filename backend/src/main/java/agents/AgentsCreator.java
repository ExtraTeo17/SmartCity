package agents;

import agents.abstractions.IAgentsContainer;
import agents.abstractions.IAgentsFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.web.LightManagersReadyEvent;
import events.web.PrepareSimulationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.abstractions.ILightAccessManager;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.buses.Timetable;
import osmproxy.buses.abstractions.IBusLinesManager;
import osmproxy.buses.data.BusPreparationData;
import osmproxy.elements.OSMContainer;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMStation;
import routing.abstractions.IRouteGenerator;
import routing.core.IGeoPosition;
import smartcity.SimulationState;
import smartcity.TimeProvider;
import smartcity.config.ConfigContainer;
import smartcity.config.StaticConfig;

import java.time.LocalDateTime;
import java.util.Collection;

public class AgentsCreator {
    private static final Logger logger = LoggerFactory.getLogger(AgentsCreator.class);
    private final IAgentsContainer agentsContainer;
    private final ConfigContainer configContainer;
    private final IBusLinesManager busLinesManager;
    private final IAgentsFactory factory;
    private final EventBus eventBus;
    private final ILightAccessManager lightAccessManager;
    private final IMapAccessManager mapAccessManager;
    private final IRouteGenerator routeGenerator;
    private final OSMContainer osmContainer;

    @Inject
    public AgentsCreator(IAgentsContainer agentsContainer,
                         ConfigContainer configContainer,
                         IBusLinesManager busLinesManager,
                         IAgentsFactory factory,
                         EventBus eventBus,
                         ILightAccessManager lightAccessManager,
                         IMapAccessManager mapAccessManager,
                         IRouteGenerator routeGenerator,
                         OSMContainer osmContainer) {
        this.agentsContainer = agentsContainer;
        this.configContainer = configContainer;
        this.busLinesManager = busLinesManager;
        this.factory = factory;
        this.eventBus = eventBus;
        this.lightAccessManager = lightAccessManager;
        this.mapAccessManager = mapAccessManager;
        this.routeGenerator = routeGenerator;
        this.osmContainer = osmContainer;
    }


    @SuppressWarnings("FeatureEnvy")
    @Subscribe
    public void handle(PrepareSimulationEvent e) {
        logger.info("Set zone event occurred: " + e.toString());
        if (configContainer.getSimulationState() == SimulationState.READY_TO_RUN) {
            agentsContainer.clearAll();
        }
        configContainer.setZone(e.zone);
        configContainer.setSimulationState(SimulationState.IN_PREPARATION);

        if (prepareAgents()) {
            configContainer.setSimulationState(SimulationState.READY_TO_RUN);
        }
    }

    private boolean prepareAgents() {
        if (configContainer.shouldGeneratePedestriansAndBuses()) {
            if (!prepareStationsAndBuses()) {
                return false;
            }
        }
        return prepareLightManagers();
    }

    private boolean prepareStationsAndBuses() {
        logger.info("Starting bus data preparation.");
        long time = System.nanoTime();
        var busData = busLinesManager.getBusData();
        logger.info("Bus data preparation finished! Took: " + TimeProvider.getTimeInMs(time) + "ms");

        logger.info("Stations creation started.");
        time = System.nanoTime();
        if (!prepareStations(busData.stations.values())) {
            return false;
        }
        logger.info("Stations are created! Took: " + TimeProvider.getTimeInMs(time) + "ms");

        logger.info("Buses creation started.");
        time = System.nanoTime();
        if (!preparesBuses(busData)) {
            return false;
        }
        logger.info("Buses are created! Took: " + TimeProvider.getTimeInMs(time) + "ms");

        return true;
    }

    private boolean preparesBuses(BusPreparationData busData) {
        int busCount = 0;
        var closestTime = LocalDateTime.now().plusDays(1);
        var currTime = LocalDateTime.now();
        for (var busInfo : busData.busInfos) {
            var timeNow = System.nanoTime();
            var routeInfo = routeGenerator.generateRouteInfoForBuses(
                    busInfo.route, busInfo.stops);
            logger.info("Generating routeInfo finished. Took: " + (TimeProvider.getTimeInMs(timeNow)) + "ms");

            var busLine = busInfo.busLine;
            for (var brigade : busInfo) {
                var brigadeNr = brigade.brigadeId;
                for (Timetable timetable : brigade) {
                    BusAgent agent = factory.create(routeInfo, timetable, busLine, brigadeNr);
                    boolean result = agentsContainer.tryAdd(agent);
                    if (result) {
                        ++busCount;
                    }
                    else {
                        logger.warn("Bus agent could not be added");
                    }

                    var startTime = timetable.getBoardingTime();
                    closestTime = TimeProvider.getCloser(currTime, closestTime, startTime);
                }
            }
        }

        if (busCount == 0) {
            logger.error("No buses were created");
            return false;
        }
        logger.info("Closest startTime: " + closestTime.toLocalTime());
        logger.info("NUMBER OF BUS AGENTS: " + busCount);
        return true;
    }

    private boolean prepareStations(Collection<? extends IGeoPosition> stationPositions) {
        int stationsCount = 0;
        for (var stationPos : stationPositions) {
            StationAgent agent = factory.create(stationPos);
            boolean result = agentsContainer.tryAdd(agent);
            if (result) {
                ++stationsCount;
                agent.start();
            }
            else {
                logger.info("Station agent could not be added");
            }
        }

        if (stationsCount == 0) {
            logger.error("No stations were created");
            return false;
        }

        logger.info("NUMBER OF STATION AGENTS: " + stationsCount);
        return true;
    }

    private boolean prepareLightManagers() {
        if (!configContainer.tryLockLightManagers()) {
            logger.error("Light managers are locked, cannot prepare.");
            return false;
        }

        logger.info("LightManagers construction started.");
        long time = System.nanoTime();
        boolean result = tryConstructLightManagers();
        logger.info("LightManagers construction finished! Took: " + TimeProvider.getTimeInMs(time) + "ms");

        configContainer.unlockLightManagers();

        if (result) {
            // TODO: Maybe only positions?
            var lightManagers = ImmutableList.copyOf(agentsContainer.iterator(LightManagerAgent.class));
            eventBus.post(new LightManagersReadyEvent(lightManagers));
        }

        return result;
    }

    @VisibleForTesting
    boolean tryConstructLightManagers() {
        try {
            if (StaticConfig.USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS) {
                var nodes = mapAccessManager.getLightManagersNodes(configContainer.getZone());
                for (var node : nodes) {
                    var manager = factory.create(node);
                    if (!agentsContainer.tryAdd(manager)) {
                        return false;
                    }
                }
            }
            else {
                var lights = lightAccessManager.getLightsOfTypeA();
                for (final OSMNode centerCrossroad : lights) {
                    if (centerCrossroad.determineParentOrientationsTowardsCrossroad()) {
                        var manager = factory.create(centerCrossroad);
                        if (!agentsContainer.tryAdd(manager)) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error preparing light managers", e);
            return false;
        }


        return true;
    }
}
