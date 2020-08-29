package agents;

import agents.abstractions.IAgentsContainer;
import agents.abstractions.IAgentsFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.LightManagersReadyEvent;
import events.PrepareSimulationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.LightAccessManager;
import osmproxy.MapAccessManager;
import osmproxy.buses.IBusLinesManager;
import osmproxy.elements.OSMNode;
import smartcity.SimulationState;
import smartcity.TimeProvider;
import smartcity.buses.Timetable;
import smartcity.config.ConfigContainer;

public class AgentsCreator {
    private static final Logger logger = LoggerFactory.getLogger(AgentsCreator.class);
    private final IAgentsContainer agentsContainer;
    private final ConfigContainer configContainer;
    private final IBusLinesManager busLinesManager;
    private final IAgentsFactory factory;
    private final EventBus eventBus;
    private final LightAccessManager lightAccessManager;

    @Inject
    public AgentsCreator(IAgentsContainer agentsContainer,
                         ConfigContainer configContainer,
                         IBusLinesManager busLinesManager,
                         IAgentsFactory factory,
                         EventBus eventBus,
                         LightAccessManager lightAccessManager) {
        this.agentsContainer = agentsContainer;
        this.configContainer = configContainer;
        this.busLinesManager = busLinesManager;
        this.factory = factory;
        this.eventBus = eventBus;
        this.lightAccessManager = lightAccessManager;
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
        int stationsCount = 0;
        for (var station : busData.stations.values()) {
            var agent = factory.create(station);
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

        logger.info("Stations are created! Took: " + TimeProvider.getTimeInMs(time) + "ms");
        logger.info("NUMBER OF STATION AGENTS: " + stationsCount);

        logger.info("Buses creation started.");
        time = System.nanoTime();
        int busCount = 0;
        for (var busInfo : busData.busInfos) {
            var timeNow = System.nanoTime();
            var routeInfo = busInfo.generateRouteInfo();
            logger.info("Generating routeInfo finished. Took: " + (TimeProvider.getTimeInMs(timeNow)) + "ms");

            var busLine = busInfo.getBusLine();
            for (var brigade : busInfo) {
                var brigadeNr = brigade.getBrigadeNr();
                for (Timetable timetable : brigade) {
                    var agent = factory.create(routeInfo, timetable, busLine, brigadeNr);
                    boolean result = agentsContainer.tryAdd(agent);
                    if (result) {
                        ++busCount;
                    }
                    else {
                        logger.warn("Bus agent could not be added");
                    }
                }
            }
        }

        if (busCount == 0) {
            logger.error("No buses were created");
            return false;
        }

        logger.info("Buses are created! Took: " + TimeProvider.getTimeInMs(time) + "ms");
        logger.info("NUMBER OF BUS AGENTS: " + busCount);
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
            var lightManagers = ImmutableList.copyOf(agentsContainer.iterator(LightManager.class));
            eventBus.post(new LightManagersReadyEvent(lightManagers));
        }

        return result;
    }

    private boolean tryConstructLightManagers() {
        try {
            if (configContainer.useDeprecatedXmlForLightManagers) {
                var nodes = MapAccessManager.getLightManagersNodes(configContainer.getZone());
                for (var node : nodes) {
                    var manager = factory.create(node);
                    if (!agentsContainer.tryAdd(manager)) {
                        return false;
                    }
                }
            }
            else {
                var lights = lightAccessManager.getLights();
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
