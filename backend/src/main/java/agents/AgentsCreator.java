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
import routing.RouteNode;
import smartcity.SimulationState;
import smartcity.buses.Timetable;
import smartcity.config.ConfigContainer;

import java.util.List;

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
        int busCount = 0;
        for (var busInfo : busLinesManager.getBusInfos()) {
            List<RouteNode> route = busInfo.getRouteInfo();
            var busLine = busInfo.getBusLine();
            for (var brigade : busInfo) {
                var brigadeNr = brigade.getBrigadeNr();
                for (Timetable timetable : brigade) {
                    var agent = factory.create(route, timetable, busLine, brigadeNr);
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

        logger.info("Buses are created!");
        logger.info("NUMBER OF BUS AGENTS: " + busCount);
        return true;
    }

    private boolean prepareLightManagers() {
        if (!configContainer.tryLockLightManagers()) {
            logger.error("Light managers are locked, cannot prepare.");
            return false;
        }

        boolean result = tryConstructLightManagers();

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
