package agents.singletons;

import agents.TroubleManagerAgent;
import com.google.inject.Inject;
import jade.core.Agent;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.SmartCityAgent;

public class SingletonAgentsActivator {
    private static final Logger logger = LoggerFactory.getLogger(SingletonAgentsActivator.class);
    private final ContainerController controller;

    @Inject
    public SingletonAgentsActivator(ContainerController controller,
                                    SmartCityAgent smartCityAgent,
                                    TroubleManagerAgent troubleManagerAgent) {
        this.controller = controller;
        activate(SmartCityAgent.name, smartCityAgent);
        activate(TroubleManagerAgent.name, troubleManagerAgent);
    }

    public void activate(String name, Agent agent){
        try {
            var agentController = controller.acceptNewAgent(name, agent);
            agentController.activate();
            agentController.start();
        } catch (StaleProxyException e) {
            logger.error("Error accepting main agent", e);
            System.exit(-1);
        }
    }

}
