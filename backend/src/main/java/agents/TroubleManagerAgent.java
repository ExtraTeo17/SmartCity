package agents;

import agents.abstractions.IAgentsContainer;
import agents.utilities.MessageParameter;
import com.google.inject.Inject;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import routing.core.Position;

import static agents.message.MessageManager.createProperties;

public class TroubleManagerAgent extends Agent {
    public static final String name = TroubleManagerAgent.class.getSimpleName().replace("Agent", "");
    private final IAgentsContainer agentsContainer;

    @Inject
    TroubleManagerAgent(IAgentsContainer agentsContainer) {
        this.agentsContainer = agentsContainer;
    }


    //TODO: wysłać broadcact kiedy trouble się skończy
    Behaviour communication = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage rcv = receive();
            if (rcv != null) {
                switch (rcv.getPerformative()) {
                    case ACLMessage.INFORM -> {

                        //parsing received message
                        //TODO: Show trouble point on gui
                        var troublePoint = Position.of(Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT)),
                                Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON)));
                        System.out.println("TroubleAgent: Got message about trouble");
                        System.out.println("troublePoint: " + troublePoint.getLat() + "  " + troublePoint.getLng());
                        Long edgeId = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
                        System.out.println("trouble edge: " + edgeId);
                        //broadcasting to everybody
                        ACLMessage response = new ACLMessage(ACLMessage.PROPOSE);
                        Properties properties = createProperties(MessageParameter.TROUBLE_MANAGER);
                        properties.setProperty(MessageParameter.EDGE_ID, edgeId.toString());
                        response.setAllUserDefinedParameters(properties);

                        agentsContainer.forEach(VehicleAgent.class, vehicleAgent -> {
                            response.addReceiver(vehicleAgent.getAID());
                        });
                        send(response);
                        System.out.println("Trouble Manger: send broadcast");

                    }
                }
                block(100);
            }
        }
    };
}
