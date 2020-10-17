package agents.message;

import agents.utilities.MessageParameter;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;

import java.util.List;

public class MessageManager {
    public static ACLMessage createMessage(int type, String receiverName) {
        var receiver = new AID(receiverName, AID.ISLOCALNAME);
        return createMessage(type, receiver);
    }

    public static ACLMessage createMessage(int type, AID receiver) {
        ACLMessage msg = new ACLMessage(type);
        msg.addReceiver(receiver);
        return msg;
    }

    public static ACLMessage createMessage(int type, List<String> receivers) {
        ACLMessage msg = new ACLMessage(type);
        for (var name : receivers) {
            msg.addReceiver(new AID(name, AID.ISLOCALNAME));
        }
        return msg;
    }

    public static Properties createProperties(String senderType) {
        var result = new Properties();
        result.setProperty(MessageParameter.TYPE, senderType);
        return result;
    }

    public static String getSender(ACLMessage rcv) {
        return rcv.getSender().getLocalName();
    }
}
