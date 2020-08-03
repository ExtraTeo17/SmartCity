package agents;

import agents.utils.MessageParameter;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import jade.wrapper.ControllerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.LightManagerNode;
import smartcity.MasterAgent;
import vehicles.MovingObject;

import java.time.Instant;

public abstract class AbstractAgent extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(AbstractAgent.class);
    private final int id;

    public AbstractAgent(int id) {
        this.id = id;
    }
    
    public String getPredictedName() {
        return getNamePrefix() + id;
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

    protected void findNextStop(MovingObject movingObject) {
        // finds next traffic light and announces his arrival
        LightManagerNode nextManager = movingObject.findNextTrafficLight();
        if (nextManager != null) {
            ACLMessage msg = prepareMessage(nextManager, movingObject);
            send(msg);
            print("Sending INFORM to LightManager" + nextManager.getLightManagerId() + ".");
        }
    }

    protected ACLMessage prepareMessage(LightManagerNode nextManager, MovingObject movingObject) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        AID dest = new AID("LightManager" + nextManager.getLightManagerId(), AID.ISLOCALNAME);
        msg.addReceiver(dest);

        Properties properties = new Properties();
        var agentType = MessageParameter.GetTypeByMovingObject(movingObject);
        properties.setProperty(MessageParameter.TYPE, agentType);
        Instant time = MasterAgent.getSimulationTime().toInstant().plusMillis(movingObject.getMillisecondsToNextLight());
        properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + time);
        properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, "" + nextManager.getOsmWayId());
        msg.setAllUserDefinedParameters(properties);

        return msg;
    }

    public void takeDown() {
        super.takeDown();
    }
}
