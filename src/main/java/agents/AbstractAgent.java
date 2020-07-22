package agents;

import jade.core.Agent;
import jade.wrapper.ControllerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAgent extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(AbstractAgent.class);

    public void start() {
        try {
            this.getContainerController().getAgent(getLocalName()).start();
        } catch (ControllerException e) {
            logger.warn("Error activating agent");
        }
    }
}
