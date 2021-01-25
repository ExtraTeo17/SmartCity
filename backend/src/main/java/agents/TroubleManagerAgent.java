package agents;

import agents.abstractions.IAgentsContainer;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.web.DebugEvent;
import events.web.roadblocks.TrafficJamFinishedEvent;
import events.web.roadblocks.TroublePointCreatedEvent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.routes.abstractions.IGraphHopper;
import routing.core.Position;
import smartcity.SimulationState;
import smartcity.config.ConfigContainer;
import utilities.ConditionalExecutor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static agents.AgentConstants.DEFAULT_BLOCK_ON_ERROR;
import static agents.message.MessageManager.createProperties;
import static agents.utilities.BehaviourWrapper.wrapErrors;

/**
 * There is one TroubleManager agent in the system. It is an agent, which manages
 * trouble places and traffic jams in the system. In the field of construction/accidents places management,
 * the agent receives information from an affected car agent about the trouble place location,
 * which was detected by this car agent on its route. The TroubleManager agent then makes a broadcast
 * to all the car agents to inform them about the incidence of the reported trouble point, so that they
 * can act accordingly. In the field of traffic jam management, the agent receives information about the
 * traffic jam from the car agent about the traffic jam location, which it further broadcasts
 * to all the cars analogically. It could also receive an information from the LightManager agent
 * about a mitigated traffic jam, triggering the TroubleManager agent to perform broadcast about the traffic
 * jam stop. Another responsibility of theTroubleManager agent is to perform the aforementioned
 * broadcasts periodically, so that newly created cars could also act upon trouble places/traffic jams.
 */
public class TroubleManagerAgent extends Agent {
    public static final String name = TroubleManagerAgent.class.getSimpleName().replace("Agent", "");
    private static final Logger logger = LoggerFactory.getLogger(TroubleManagerAgent.class);
    public static final int PERIOD_OF_BROADCASTING = 5000;

    private final IAgentsContainer agentsContainer;
    private final IGraphHopper graphHopper;
    private final ConfigContainer configContainer;
    private final EventBus eventBus;

    private final Map<Integer, String> mapOfLightTrafficJamBlockedEdges;
    private final Map<Integer, String> mapOfConstructionSiteBlockedEdges;
    private int latestTroublePointId;

    @Inject
    TroubleManagerAgent(IAgentsContainer agentsContainer,
                        IGraphHopper graphHopper,
                        ConfigContainer configContainer,
                        EventBus eventBus) {
        this.agentsContainer = agentsContainer;
        this.graphHopper = graphHopper;
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
            graphHopper.addForbiddenEdges(Collections.singletonList(edgeId));
        }
        sendBroadcast(generateMessageAboutTrouble(rcv, MessageParameter.CONSTRUCTION, MessageParameter.SHOW));
    }

    private void trafficJamsAppearedHandle(ACLMessage rcv) {
        int edgeId = Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
        logger.debug("Got message about light traffic jam start on: " + edgeId);
        if (!mapOfLightTrafficJamBlockedEdges.containsKey(edgeId)) {
            mapOfLightTrafficJamBlockedEdges.put(edgeId, rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM));
            graphHopper.addForbiddenEdges(Collections.singletonList(edgeId));
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
            graphHopper.removeForbiddenEdges(Collections.singletonList(edgeId));
        }
        sendBroadcast(generateMessageAboutTrouble(rcv, MessageParameter.TRAFFIC_JAM, MessageParameter.STOP));
    }

    // TODO: Send broadcasts when the trouble ends
    @Override
    protected void setup() {

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


        Behaviour sayAboutTroubles = new TickerBehaviour(this, PERIOD_OF_BROADCASTING) {
            @Override
            protected void onTick() {

                if (configContainer.getSimulationState() != SimulationState.RUNNING) {
                    return;
                }
                for (Map.Entry<Integer, String> entry : mapOfConstructionSiteBlockedEdges.entrySet()) {
                    // construction site
                    ConditionalExecutor.debug(() ->
                            logger.info("Edge id blocked: " + entry.getKey() + " length of jam: " + entry.getValue()));
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

        Consumer<Exception> onError = e -> {
            logger.error("Terminating!", e);
            doDelete();
        };
        addBehaviour(wrapErrors(communication, onError));
        addBehaviour(wrapErrors(sayAboutTroubles, onError));
    }

    private void handleBusAccident(ACLMessage rcv) {

    }

    @Subscribe
    void handle(DebugEvent e) {
    }
}
