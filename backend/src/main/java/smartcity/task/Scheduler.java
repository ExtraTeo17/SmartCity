package smartcity.task;

import agents.LightManagerAgent;
import agents.abstractions.AbstractAgent;
import agents.abstractions.IAgentsContainer;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.ClearSimulationEvent;
import events.StartTimeEvent;
import events.SwitchLightsStartEvent;
import events.web.SimulationStartedEvent;
import events.web.StartSimulationEvent;
import smartcity.SimulationState;
import smartcity.config.ConfigContainer;
import smartcity.task.abstractions.ITaskManager;

public class Scheduler {
    private final ITaskManager taskManager;
    private final ConfigContainer configContainer;
    private final IAgentsContainer agentsContainer;
    private final EventBus eventBus;

    @Inject
    public Scheduler(ITaskManager taskManager,
                     ConfigContainer configContainer,
                     IAgentsContainer agentsContainer,
                     EventBus eventBus) {
        this.taskManager = taskManager;
        this.configContainer = configContainer;
        this.agentsContainer = agentsContainer;
        this.eventBus = eventBus;
    }

    @SuppressWarnings("FeatureEnvy")
    @Subscribe
    public void handle(StartSimulationEvent e) {
        activateLightManagerAgents();
        if (configContainer.shouldGenerateCars()) {
            taskManager.scheduleCarCreation(e.carsNum, e.testCarId);
        }
        // TODO: Add bikes limit and testBikeID
        if(configContainer.shouldGenerateBikes()){
            taskManager.scheduleBikeCreation(20, 10);
        }
        if (configContainer.shouldGeneratePedestriansAndBuses()) {
            // TODO: Add pedestrians limit and testPedestrianID
            taskManager.schedulePedestrianCreation(50, e.testCarId);
            taskManager.scheduleBusControl(() -> configContainer.getSimulationState() == SimulationState.RUNNING);
        }

        configContainer.setSimulationState(SimulationState.RUNNING);
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
    public void handle(StartTimeEvent e) {
        taskManager.scheduleSimulationControl(() -> configContainer.getSimulationState() == SimulationState.RUNNING,
                e.timePosted);
    }

    @Subscribe
    public void handle(ClearSimulationEvent e){
        taskManager.cancelAll();
    }
}
