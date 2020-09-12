package agents.abstractions;

import agents.LightManagerAgent;
import agents.utilities.LoggerLevel;
import agents.utilities.MessageParameter;
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
import java.util.List;

public abstract class AbstractAgent extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(AbstractAgent.class);
    private final int id;

    public AbstractAgent(int id) {
        this.id = id;
    }

    public String getPredictedName() {
        return getPredictedName(getNamePrefix(), id);
    }

    protected String getPredictedName(String prefix, int id) {
        return prefix.concat(Integer.toString(id));
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

    public void print(String message, LoggerLevel level) {
        var fullMsg = getLocalName() + ": " + message;
        switch (level) {
            case TRACE -> logger.trace(fullMsg);
            case DEBUG -> logger.debug(fullMsg);
            case INFO -> logger.info(fullMsg);
            case WARN -> logger.warn(fullMsg);
            case ERROR -> logger.error(fullMsg);
        }
    }

    // TODO: Protected
    public void print(String message) {
        print(message, LoggerLevel.INFO);
    }

    // TODO: Pass only LightManager here, remove movingObject and pass additional parameters
    protected void informLightManager(MovingObject movingObject) {
        // finds next traffic light and announces his arrival
        LightManagerNode nextManager = movingObject.getNextTrafficLight();
        if (nextManager != null) {
            ACLMessage msg = prepareMessageForManager(nextManager, movingObject);
            send(msg);
            print("Sending INFORM to LightManager" + nextManager.getLightManagerId() + ".");
        }
    }

    private ACLMessage prepareMessageForManager(LightManagerNode managerNode, MovingObject movingObject) {
        ACLMessage msg = createMessage(ACLMessage.INFORM, LightManagerAgent.name, managerNode.getLightManagerId());

        Properties properties = new Properties();
        var agentType = MessageParameter.getTypeByMovingObject(movingObject);
        properties.setProperty(MessageParameter.TYPE, agentType);
        Instant time = MasterAgent.getSimulationTime().toInstant().plusMillis(movingObject.getMillisecondsToNextLight());
        properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + time);
        properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, "" + managerNode.getOsmWayId());
        msg.setAllUserDefinedParameters(properties);

        return msg;
    }

    // TODO: Special class - MessageCreator for all msg-related code, protected, dependency, injected
    protected ACLMessage createMessage(int type, List<String> receivers) {
        ACLMessage msg = new ACLMessage(type);
        for(var name : receivers){
            msg.addReceiver(new AID(name, AID.ISLOCALNAME));
        }
        return msg;
    }

    protected ACLMessage createMessage(int type, String receiverName) {
        var receiver = new AID(receiverName, AID.ISLOCALNAME);
        return createMessage(type, receiver);
    }

    protected ACLMessage createMessage(int type, String receiverName, int receiverId) {
        var receiver = new AID(getPredictedName(receiverName, receiverId), AID.ISLOCALNAME);
        return createMessage(type, receiver);
    }

    protected ACLMessage createMessage(int type, AID receiver) {
        ACLMessage msg = new ACLMessage(type);
        msg.addReceiver(receiver);
        return msg;
    }
}
