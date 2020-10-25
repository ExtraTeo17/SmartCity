package agents;

import agents.abstractions.IAgentsContainer;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.web.PrepareSimulationEvent;
import events.web.TroublePointCreatedEvent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.core.Position;

import java.util.*;

import static agents.message.MessageManager.createProperties;

public class TroubleManagerAgent extends Agent {
    public static final String name = TroubleManagerAgent.class.getSimpleName().replace("Agent", "");
    private final static Logger logger = LoggerFactory.getLogger(TroubleManagerAgent.class);

    private final IAgentsContainer agentsContainer;
    private final EventBus eventBus;
    private Map<Integer,String> mapOfBlockedEdges = new HashMap<Integer,String>();
    @Inject
    TroubleManagerAgent(IAgentsContainer agentsContainer,
                        EventBus eventBus) {
        this.agentsContainer = agentsContainer;
        this.eventBus = eventBus;
    }
    private void trafficJamsAppearedHandle(ACLMessage rcv) {
        //TODO: Rysowanie w LightManager - Przemek
        Position positionOfTroubleLight = Position.of(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT),
                rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON));
        logger.info("Got message about trouble - TRAFFIC JAM");
        int edgeId = Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
        //  if (!setOfBlockedEdges.contains(edgeId)) { // TODO: REMEMBER TO UNCOMMENT
        mapOfBlockedEdges.put(edgeId,rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM));
        sendBroadcast(generateMessageAboutTrouble(rcv, MessageParameter.TRAFFIC_JAMS));
        // }

    }

    private void sendBroadcast(ACLMessage response) {
        agentsContainer.forEach(VehicleAgent.class, vehicleAgent -> {
            response.addReceiver(vehicleAgent.getAID());
        });
        send(response);
        logger.info("Sent broadcast");
    }
    private ACLMessage generateMessageAboutTrouble( ACLMessage rcv, String typeOfTrouble)
    {
        long edgeId = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
        logger.info("trouble edge: " + edgeId);
        //broadcasting to everybody
        ACLMessage response = new ACLMessage(ACLMessage.PROPOSE);
        Properties properties = createProperties(MessageParameter.TROUBLE_MANAGER);
        properties.setProperty(MessageParameter.EDGE_ID, Long.toString(edgeId));
        properties.setProperty(MessageParameter.TYPEOFTROUBLE, typeOfTrouble);
        if(typeOfTrouble.equals(MessageParameter.TRAFFIC_JAMS))
        {
            properties.setProperty(MessageParameter.LENGTH_OF_JAM, rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM));
        }
        response.setAllUserDefinedParameters(properties);
        return response;
    }
    private ACLMessage generateMessageAbouTrafficJam( int edgeId, String lengthOfJam)
    {

        logger.info("trouble edge: " + edgeId);
        //broadcasting to everybody
        ACLMessage response = new ACLMessage(ACLMessage.PROPOSE);
        Properties properties = createProperties(MessageParameter.TROUBLE_MANAGER);
        properties.setProperty(MessageParameter.EDGE_ID, Long.toString(edgeId));
        properties.setProperty(MessageParameter.TYPEOFTROUBLE, MessageParameter.TRAFFIC_JAMS);
        properties.setProperty(MessageParameter.LENGTH_OF_JAM, lengthOfJam);
        response.setAllUserDefinedParameters(properties);
        return response;
    }


    private void constructionAppearedHandle(ACLMessage rcv) {
        var troublePoint = Position.of(Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT)),
                Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON)));
        // TODO: Generate id and save it to hide troublePoint later
        eventBus.post(new TroublePointCreatedEvent(1, troublePoint));
        logger.info("Got message about trouble - CONSTRUCTION");
        logger.info("troublePoint: " + troublePoint.getLat() + "  " + troublePoint.getLng());
        sendBroadcast(generateMessageAboutTrouble(rcv,MessageParameter.CONSTRUCTION));

    }
    @Override
    protected void setup() {

        //TODO: wysłać broadcact kiedy trouble się skończy
        Behaviour communication = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv != null) {
                    switch (rcv.getPerformative()) {
                        case ACLMessage.INFORM -> {

                            if (rcv.getUserDefinedParameter(MessageParameter.TROUBLE).equals(MessageParameter.SHOW)) {
                                //parsing received message
                                if (rcv.getUserDefinedParameter(MessageParameter.TYPEOFTROUBLE).equals(MessageParameter.CONSTRUCTION)) {
                                      constructionAppearedHandle(rcv);
                                }
                                else if(rcv.getUserDefinedParameter(MessageParameter.TYPEOFTROUBLE).equals(MessageParameter.TRAFFIC_JAMS)) {
                                    trafficJamsAppearedHandle(rcv);
                                }
                            }
                            else if (rcv.getUserDefinedParameter(MessageParameter.TROUBLE).equals(MessageParameter.STOP)) {
                                //TODO: FOR FUTURE CHANGE ROOT AGAIN OF THE CAR?
                            }
                        }
                    }
                }
                block(100);
            }







        };
        var sayAboutJam = new TickerBehaviour(this, 1000) {//100 / TimeProvider.TIME_SCALE) {
            @Override
            protected void onTick() {
                for (Map.Entry<Integer, String> entry : mapOfBlockedEdges.entrySet()) {
                    sendBroadcast(generateMessageAbouTrafficJam(entry.getKey(), entry.getValue()));
                    }

            }
        };
        addBehaviour(communication);
        addBehaviour(sayAboutJam);
    }

    // for tests
    @Subscribe
    public void handle(PrepareSimulationEvent e) {
        var troublePoint = Position.of(52.23682, 21.01683);
        eventBus.post(new TroublePointCreatedEvent(1, troublePoint));
    }
}
