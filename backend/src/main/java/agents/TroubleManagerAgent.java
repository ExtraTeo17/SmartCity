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
import osmproxy.ExtendedGraphHopper;

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
    private Map<Integer, String> mapOfLightTrafficJamBlockedEdges = new HashMap<Integer, String>();
    private Map<Integer, String> mapOfConstructionSiteBlockedEdges = new HashMap<Integer, String>();
    
    @Inject
    TroubleManagerAgent(IAgentsContainer agentsContainer,
                        EventBus eventBus) {
        this.agentsContainer = agentsContainer;
        this.eventBus = eventBus;
    }

    private void sendBroadcast(ACLMessage response) {
        agentsContainer.forEach(VehicleAgent.class, vehicleAgent -> {
            response.addReceiver(vehicleAgent.getAID());
        });
        send(response);
        logger.info("Sent broadcast");
    }
    
    private ACLMessage generateMessageAboutTrouble(ACLMessage rcv, String typeOfTrouble, String showOrStop) {
        int edgeId = Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
        String lengthOfJam = null;
        if (rcv.getAllUserDefinedParameters().contains(MessageParameter.LENGTH_OF_JAM)) {
            lengthOfJam = rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM);
        }
        return generateMessageAboutTrafficJam(edgeId, lengthOfJam, typeOfTrouble,showOrStop);
    }
    
    private ACLMessage generateMessageAboutTrafficJam(int edgeId, String lengthOfJam, String typeOfTrouble, String showOrStop) {
        logger.info("Got message about trouble on edge: " + edgeId); // broadcasting to everybody
        ACLMessage response = new ACLMessage(ACLMessage.PROPOSE);
        Properties properties = createProperties(MessageParameter.TROUBLE_MANAGER);
        properties.setProperty(MessageParameter.EDGE_ID, Long.toString(edgeId));
        properties.setProperty(MessageParameter.TYPEOFTROUBLE, typeOfTrouble);
        properties.setProperty(MessageParameter.TROUBLE,showOrStop);
        if (lengthOfJam != null) {
        	properties.setProperty(MessageParameter.LENGTH_OF_JAM, lengthOfJam);
        }
        response.setAllUserDefinedParameters(properties);
        return response;
    }

    private void constructionAppearedHandle(ACLMessage rcv) {
        int edgeId = Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
        var troublePoint = Position.of(Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT)),
                Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON)));
        // TODO: Generate id and save it to hide troublePoint later
        eventBus.post(new TroublePointCreatedEvent(1, troublePoint));
        logger.info("Got message about trouble - CONSTRUCTION");
        logger.info("troublePoint: " + troublePoint.getLat() + "  " + troublePoint.getLng());
        if (!mapOfConstructionSiteBlockedEdges.containsKey(edgeId)) {
        	mapOfConstructionSiteBlockedEdges.put(edgeId, rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM));
            ExtendedGraphHopper.addForbiddenEdges(Arrays.asList(edgeId));
        }
        sendBroadcast(generateMessageAboutTrouble(rcv, MessageParameter.CONSTRUCTION,MessageParameter.SHOW));
    }
    
    private void trafficJamsAppearedHandle(ACLMessage rcv) {
        //TODO: Rysowanie w LightManager - Przemek
        Position positionOfTroubleLight = Position.of(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT),
                rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON));
        int edgeId = Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
        logger.info("Got message about light traffic jam start on: " + edgeId);
        if (!mapOfLightTrafficJamBlockedEdges.containsKey(edgeId)) {
        	mapOfLightTrafficJamBlockedEdges.put(edgeId, rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM));
            ExtendedGraphHopper.addForbiddenEdges(Arrays.asList(edgeId));
        }
        sendBroadcast(generateMessageAboutTrouble(rcv, MessageParameter.TRAFFIC_JAMS,MessageParameter.SHOW));
        // }
    }

	private void trafficJamsDisappearedHandle(ACLMessage rcv) {
        //TODO: Rysowanie w LightManager - Przemek
		Position positionOfTroubleLight = Position.of(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT),
                rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON));
        int edgeId = Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
        logger.info("Got message about light traffic jam stop on: " + edgeId);
        if (!mapOfLightTrafficJamBlockedEdges.containsKey(edgeId)) {
        	mapOfLightTrafficJamBlockedEdges.remove(edgeId);
            ExtendedGraphHopper.removeForbiddenEdges(Arrays.asList(edgeId));
        }
        sendBroadcast(generateMessageAboutTrouble(rcv, MessageParameter.TRAFFIC_JAMS,MessageParameter.STOP));
	}
    
    @Override
    protected void setup() { // TODO: wysłać broadcact kiedy trouble się skończy
    	
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
                                } else if (rcv.getUserDefinedParameter(MessageParameter.TYPEOFTROUBLE).equals(MessageParameter.TRAFFIC_JAMS)) {
                                    trafficJamsAppearedHandle(rcv);
                                }
                            } else if (rcv.getUserDefinedParameter(MessageParameter.TROUBLE).equals(MessageParameter.STOP)) {
                                trafficJamsDisappearedHandle(rcv);
                            }
                        }
                    }
                }
                block(100);
            }
        };
        
        var sayAboutJam = new TickerBehaviour(this, 2000) {//100 / TimeProvider.TIME_SCALE) {
            @Override
            protected void onTick() {
                for (Map.Entry<Integer, String> entry : mapOfLightTrafficJamBlockedEdges.entrySet()) {
                    sendBroadcast(generateMessageAboutTrafficJam(entry.getKey(), entry.getValue(),
                                                                    MessageParameter.TRAFFIC_JAMS,MessageParameter.SHOW));
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
