package agents.singletons;

import agents.TroubleManagerAgent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.web.SimulationStartedEvent;
import jade.core.Agent;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.SmartCityAgent;

/**
 * Used to dynamically activate {@link SmartCityAgent} and {@link TroubleManagerAgent}. <br/>
 * {@link SmartCityAgent} is activated along with this entity creation, i.e. in constructor.  <br/>
 * {@link TroubleManagerAgent} is activated on {@link SimulationStartedEvent}, because
 * he needs simulation `timeScale` value, so activation is possible only after simulation start. <br/>
 */
public class SingletonAgentsActivator {
    private static final Logger logger = LoggerFactory.getLogger(SingletonAgentsActivator.class);
    private final ContainerController controller;
    private final TroubleManagerAgent troubleManagerAgent;
    private final EventBus eventBus;

    /**
     * Initialize the activator of agents, which have only one instance in the smart city system.
     *
     * @param controller          Controller of the container to which the agents are to be added
     * @param eventBus            Event bus for which the events are to be dispatched
     * @param smartCityAgent      Instance of {@link SmartCityAgent} to be added to the container
     * @param troubleManagerAgent Instance of {@link TroubleManagerAgent} to be added to the container
     */
    @Inject
    public SingletonAgentsActivator(ContainerController controller,
                                    EventBus eventBus,
                                    SmartCityAgent smartCityAgent,
                                    TroubleManagerAgent troubleManagerAgent) {
        this.controller = controller;
        this.eventBus = eventBus;

        this.troubleManagerAgent = troubleManagerAgent;
        activate(SmartCityAgent.name, smartCityAgent);
    }

    private void activate(String name, Agent agent) {
        try {
            var agentController = controller.acceptNewAgent(name, agent);
            agentController.activate();
            agentController.start();
        } catch (StaleProxyException e) {
            logger.error("Error accepting main agent", e);
            System.exit(-1);
        }
    }

    @Subscribe
    public void handle(SimulationStartedEvent e) {
        activate(TroubleManagerAgent.name, troubleManagerAgent);
        this.eventBus.unregister(this);
    }
}
