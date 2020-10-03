package smartcity.task;

import agents.LightManagerAgent;
import agents.abstractions.AbstractAgent;
import agents.abstractions.IAgentsContainer;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.SwitchLightsStartEvent;
import events.web.StartSimulationEvent;
import smartcity.SimulationState;
import smartcity.config.ConfigContainer;
import smartcity.task.abstractions.ITaskManager;

public class Scheduler {
    private final ITaskManager taskManager;
    private final ConfigContainer configContainer;
    private final IAgentsContainer agentsContainer;

    @Inject
    public Scheduler(ITaskManager taskManager,
                     ConfigContainer configContainer,
                     IAgentsContainer agentsContainer) {
        this.taskManager = taskManager;
        this.configContainer = configContainer;
        this.agentsContainer = agentsContainer;
    }

    @SuppressWarnings("FeatureEnvy")
    @Subscribe
    public void handle(StartSimulationEvent e) {
        activateLightManagerAgents();
        if (configContainer.shouldGenerateCars()) {
            taskManager.scheduleCarCreation(e.carsNum, e.testCarId);
        }
        if (configContainer.shouldGeneratePedestriansAndBuses()) {
            // TODO: Add pedestrians limit and testPedestrianID
            taskManager.schedulePedestrianCreation(100_000, e.testCarId);
            taskManager.scheduleBusControl(() -> configContainer.getSimulationState() == SimulationState.RUNNING);
        }

        configContainer.setSimulationState(SimulationState.RUNNING);
    }

    private void activateLightManagerAgents() {
        agentsContainer.forEach(LightManagerAgent.class, AbstractAgent::start);
    }

    @Subscribe
    public void handle(SwitchLightsStartEvent e) {
        taskManager.scheduleSwitchLightTask(e.lights);
    }
}
