package agents;

import jade.core.Agent;
import jade.wrapper.ControllerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAgent extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(AbstractAgent.class);
    private final int id;

    public AbstractAgent(int id) {
        this.id = id;
    }

    public AbstractAgent(int id, String args[]) {
        this(id);
        setArguments(args);
    }

    public String getPredictedName() {
        return getNamePrefix()+id;
    }

    public abstract String getNamePrefix();

    public int getId() {
        return id;
    }

    public void start() {
        try {
            this.getContainerController().getAgent(getLocalName()).start();
        } catch (ControllerException e) {
            logger.warn("Error activating agent", e);
        }
    }

    public void print(String message) {
        logger.info(getLocalName() + ": " + message);
    }
}
