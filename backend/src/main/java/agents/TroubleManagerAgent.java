package agents;

import agents.abstractions.IAgentsContainer;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.web.DebugEvent;
import events.web.roadblocks.TrafficJamFinishedEvent;
import events.web.roadblocks.TrafficJamStartedEvent;
import events.web.roadblocks.TroublePointCreatedEvent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.ExtendedGraphHopper;
import routing.core.Position;
import smartcity.SimulationState;
import smartcity.config.ConfigContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static agents.AgentConstants.DEFAULT_BLOCK_ON_ERROR;
import static agents.message.MessageManager.createProperties;

public class TroubleManagerAgent extends Agent {
    public static final String name = TroubleManagerAgent.class.getSimpleName().replace("Agent", "");
    private final static Logger logger = LoggerFactory.getLogger(TroubleManagerAgent.class);

    private final IAgentsContainer agentsContainer;
    private final ConfigContainer configContainer;
    private final EventBus eventBus;

    private final Map<Integer, String> mapOfLightTrafficJamBlockedEdges;
    private final Map<Integer, String> mapOfConstructionSiteBlockedEdges;
    private int latestTroublePointId;

    @Inject
    TroubleManagerAgent(IAgentsContainer agentsContainer,
                        ConfigContainer configContainer,
                        EventBus eventBus) {
        this.agentsContainer = agentsContainer;
        this.configContainer = configContainer;
        this.eventBus = eventBus;

        this.mapOfLightTrafficJamBlockedEdges = new HashMap<>();
        this.mapOfConstructionSiteBlockedEdges = new HashMap<>();
    }

    private void sendBroadcast(ACLMessage response) {
        agentsContainer.forEach(CarAgent.class, CarAgent -> response.addReceiver(CarAgent.getAID()));
        send(response);
        logger.debug("Sent broadcast");
    }

    private ACLMessage generateMessageAboutTrouble(ACLMessage rcv, String typeOfTrouble, String showOrStop) {
        int edgeId = Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
        String lengthOfJam = null;
        if (rcv.getAllUserDefinedParameters().contains(MessageParameter.LENGTH_OF_JAM)) {
            lengthOfJam = rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM);
        }
        return generateMessageAboutTrafficJam(edgeId, lengthOfJam, typeOfTrouble, showOrStop);
    }

    private ACLMessage generateMessageAboutTrafficJam(int edgeId, String lengthOfJam, String typeOfTrouble, String showOrStop) {
        logger.debug("Got message about trouble on edge: " + edgeId); // broadcasting to everybody

        ACLMessage response = new ACLMessage(ACLMessage.PROPOSE);
        Properties properties = createProperties(MessageParameter.TROUBLE_MANAGER);
        properties.setProperty(MessageParameter.EDGE_ID, Long.toString(edgeId));
        properties.setProperty(MessageParameter.TYPEOFTROUBLE, typeOfTrouble);
        properties.setProperty(MessageParameter.TROUBLE, showOrStop);
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

        eventBus.post(new TroublePointCreatedEvent(++latestTroublePointId, troublePoint));
        logger.debug("Got message about new troublePoint: " + troublePoint.getLat() + "  " + troublePoint.getLng());
        if (!mapOfConstructionSiteBlockedEdges.containsKey(edgeId)) {
            mapOfConstructionSiteBlockedEdges.put(edgeId, rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM));
            ExtendedGraphHopper.addForbiddenEdges(Collections.singletonList(edgeId));
        }
        sendBroadcast(generateMessageAboutTrouble(rcv, MessageParameter.CONSTRUCTION, MessageParameter.SHOW));
    }

    private void trafficJamsAppearedHandle(ACLMessage rcv) {
        var lat = Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT));
        var lng = Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON));
        eventBus.post(new TrafficJamStartedEvent(Position.longHash(lat, lng)));

        int edgeId = Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
        logger.debug("Got message about light traffic jam start on: " + edgeId);
        if (!mapOfLightTrafficJamBlockedEdges.containsKey(edgeId)) {
            mapOfLightTrafficJamBlockedEdges.put(edgeId, rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM));
            ExtendedGraphHopper.addForbiddenEdges(Collections.singletonList(edgeId));
        }
        else {
            mapOfLightTrafficJamBlockedEdges.replace(edgeId, rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM));
        }
        sendBroadcast(generateMessageAboutTrouble(rcv, MessageParameter.TRAFFIC_JAM, MessageParameter.SHOW));
    }

    private void trafficJamsDisappearedHandle(ACLMessage rcv) {
        var lat = Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT));
        var lng = Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON));
        eventBus.post(new TrafficJamFinishedEvent(Position.longHash(lat, lng)));

        int edgeId = Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
        logger.debug("Got message about light traffic jam stop on: " + edgeId);
        if (mapOfLightTrafficJamBlockedEdges.containsKey(edgeId)) {
            mapOfLightTrafficJamBlockedEdges.remove(edgeId);
            ExtendedGraphHopper.removeForbiddenEdges(Collections.singletonList(edgeId));
        }
        sendBroadcast(generateMessageAboutTrouble(rcv, MessageParameter.TRAFFIC_JAM, MessageParameter.STOP));
    }

    @Override
    protected void setup() { // TODO: wysłać broadcact kiedy trouble się skończy

        Behaviour communication = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv == null) {
                    block();
                    return;
                }

                switch (rcv.getPerformative()) {
                    case ACLMessage.INFORM -> {

                        if (rcv.getUserDefinedParameter(MessageParameter.TROUBLE).equals(MessageParameter.SHOW)) {
                            //parsing received message
                            if (rcv.getUserDefinedParameter(MessageParameter.TYPEOFTROUBLE).equals(MessageParameter.CONSTRUCTION)) {
                                constructionAppearedHandle(rcv);
                            }
                            else if (rcv.getUserDefinedParameter(MessageParameter.TYPEOFTROUBLE).equals(MessageParameter.TRAFFIC_JAM)) {
                                trafficJamsAppearedHandle(rcv);
                            }
                        }
                        else if (rcv.getUserDefinedParameter(MessageParameter.TROUBLE).equals(MessageParameter.STOP)) {
                            if (rcv.getUserDefinedParameter(MessageParameter.TYPEOFTROUBLE).equals(MessageParameter.TRAFFIC_JAM)) {
                                trafficJamsDisappearedHandle(rcv);
                            }
                        }
                    }

                    default -> block(DEFAULT_BLOCK_ON_ERROR);
                }
            }
        };
        addBehaviour(communication);

        Behaviour sayAboutTroubles = new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {

                if (configContainer.getSimulationState() != SimulationState.RUNNING) {
                    return;
                }
                for (Map.Entry<Integer, String> entry : mapOfConstructionSiteBlockedEdges.entrySet()) {
                    // construction site
                    logger.info("Edge id blocked: " + entry.getKey() + " length of jam: " + entry.getValue());
                    sendBroadcast(generateMessageAboutTrafficJam(entry.getKey(), entry.getValue(),
                            MessageParameter.CONSTRUCTION, MessageParameter.SHOW));
                }
                for (Map.Entry<Integer, String> entry : mapOfLightTrafficJamBlockedEdges.entrySet()) {
                    //traffic jam
                    sendBroadcast(generateMessageAboutTrafficJam(entry.getKey(), entry.getValue(),
                            MessageParameter.TRAFFIC_JAM, MessageParameter.SHOW));
                }
            }
        };
        addBehaviour(sayAboutTroubles);
    }

    @Subscribe
    void handle(DebugEvent e) {

    }
}
