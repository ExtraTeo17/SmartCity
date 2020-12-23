package agents.message;

import agents.utilities.MessageParameter;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;

import java.util.List;

/**
 * Helper class to send messages between agents
 */
public class MessageManager {
    public static ACLMessage createMessage(int type, String receiverName) {
        var receiver = new AID(receiverName, AID.ISLOCALNAME);
        return createMessage(type, receiver);
    }

    /**
     * Create message
     *
     * @param type     type of message
     * @param receiver to whom message will be sent
     * @return message with all necessary information
     */
    public static ACLMessage createMessage(int type, AID receiver) {
        ACLMessage msg = new ACLMessage(type);
        msg.addReceiver(receiver);
        return msg;
    }

    /**
     * Create message
     *
     * @param type      type of message
     * @param receivers list to whom messages will be sent
     * @return message with all necessary information
     */
    public static ACLMessage createMessage(int type, List<String> receivers) {
        ACLMessage msg = new ACLMessage(type);
        for (var name : receivers) {
            msg.addReceiver(new AID(name, AID.ISLOCALNAME));
        }
        return msg;
    }

    /**
     * @param senderType the name of sender, ex. MessageParameter.BIKE
     * @return object with properties
     */
    public static Properties createProperties(String senderType) {
        var result = new Properties();
        result.setProperty(MessageParameter.TYPE, senderType);
        return result;
    }

    public static String getSender(ACLMessage rcv) {
        return rcv.getSender().getLocalName();
    }
}
