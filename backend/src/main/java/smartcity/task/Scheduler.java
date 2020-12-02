package smartcity.task;

import agents.BusAgent;
import agents.LightManagerAgent;
import agents.abstractions.AbstractAgent;
import agents.abstractions.IAgentsContainer;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.ClearSimulationEvent;
import events.SwitchLightsStartEvent;
import events.web.SimulationStartedEvent;
import events.web.StartSimulationEvent;
import events.web.bus.BusAgentCrashedEvent;
import smartcity.ITimeProvider;
import smartcity.SimulationState;
import smartcity.config.ConfigContainer;
import smartcity.task.abstractions.ITaskManager;

public class Scheduler {
    private final ITaskManager taskManager;
    private final ConfigContainer configContainer;
    private final IAgentsContainer agentsContainer;
    private final ITimeProvider timeProvider;
    private final EventBus eventBus;

    @Inject
    public Scheduler(ITaskManager taskManager,
                     ConfigContainer configContainer,
                     IAgentsContainer agentsContainer,
                     ITimeProvider timeProvider,
                     EventBus eventBus) {
        this.taskManager = taskManager;
        this.configContainer = configContainer;
        this.agentsContainer = agentsContainer;
        this.timeProvider = timeProvider;
        this.eventBus = eventBus;
    }

    @SuppressWarnings("FeatureEnvy")
    @Subscribe
    public void handle(StartSimulationEvent e) {
        configContainer.setExtendLightTime(e.extendLightTime);
        configContainer.setLightStrategyActive(e.lightStrategyActive);
        timeProvider.setTimeScale(e.timeScale);

        activateLightManagerAgents();
        configContainer.setTrafficJamStrategyActive(
                agentsContainer.size(LightManagerAgent.class) > 0 && e.changeRouteOnTrafficJam);
        configContainer.setShouldGenerateConstructionSites(e.shouldGenerateTroublePoints);
        configContainer.setTimeBeforeTrouble(e.timeBeforeTrouble);
        configContainer.setConstructionSiteStrategyActive(e.changeRouteOnTroublePoint);
        configContainer.setUseFixedRoutes(e.useFixedRoutes);
        configContainer.setUseFixedConstructionSites(e.useFixedRoutes && e.useFixedConstructionSites);

        if (e.shouldGenerateCars) {
            taskManager.scheduleCarCreation(e.carsNum, e.testCarId);
        }

        if (e.shouldGenerateBikes) {
            taskManager.scheduleBikeCreation(e.bikesNum, e.testBikeId);
        }

        if (configContainer.shouldGeneratePedestriansAndBuses()) {
            configContainer.setStationStrategyActive(e.stationStrategyActive);
            configContainer.setExtendWaitTime(e.extendWaitTime);

            taskManager.schedulePedestrianCreation(e.pedestriansLimit, e.testPedestrianId);
            taskManager.scheduleBusControl(() -> configContainer.getSimulationState() == SimulationState.RUNNING);
        }

        configContainer.setSimulationState(SimulationState.RUNNING);
        taskManager.scheduleSimulationControl(() -> configContainer.getSimulationState() == SimulationState.RUNNING,
                e.startTime);
        eventBus.post(new SimulationStartedEvent());
    }

    private void activateLightManagerAgents() {
        agentsContainer.forEach(LightManagerAgent.class, AbstractAgent::start);
    }

    @Subscribe
    public void handle(SwitchLightsStartEvent e) {
        taskManager.scheduleSwitchLightTask(e.managerId, e.lights);
    }

    @Subscribe
    public void handle(ClearSimulationEvent e) {
        taskManager.cancelAll();
    }

    @Subscribe
    public void handle(BusAgentCrashedEvent e) {agentsContainer.remove(BusAgent.class, e.id);}
}
