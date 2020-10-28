package agents;

import agents.abstractions.IAgentsContainer;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import events.web.TroublePointCreatedEvent;
import events.web.TroublePointVanishedEvent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.ExtendedGraphHopper;
import routing.core.IGeoPosition;
import routing.core.Position;
import smartcity.SimulationState;
import smartcity.TimeProvider;
import smartcity.config.ConfigContainer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static agents.message.MessageManager.createProperties;

public class TroubleManagerAgent extends Agent {
    public static final String name = TroubleManagerAgent.class.getSimpleName().replace("Agent", "");
    private final static Logger logger = LoggerFactory.getLogger(TroubleManagerAgent.class);

    private final IAgentsContainer agentsContainer;
    private final ConfigContainer configContainer;
    private final EventBus eventBus;

    private final Map<Integer, String> mapOfLightTrafficJamBlockedEdges;
    private final Map<Integer, String> mapOfConstructionSiteBlockedEdges;
    private final HashMap<IGeoPosition, Integer> troublePointsMap;
    private int latestTroublePointId;

    @Inject
    TroubleManagerAgent(IAgentsContainer agentsContainer,
                        ConfigContainer configContainer,
                        EventBus eventBus) {
        this.agentsContainer = agentsContainer;
        this.configContainer = configContainer;
        this.eventBus = eventBus;

        this.troublePointsMap = new HashMap<>();
        this.mapOfLightTrafficJamBlockedEdges = new HashMap<>();
        this.mapOfConstructionSiteBlockedEdges = new HashMap<>();
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
        return generateMessageAboutTrafficJam(edgeId, lengthOfJam, typeOfTrouble, showOrStop);
    }

    private ACLMessage generateMessageAboutTrafficJam(int edgeId, String lengthOfJam, String typeOfTrouble, String showOrStop) {
        logger.info("Got message about trouble on edge: " + edgeId); // broadcasting to everybody
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

        troublePointsMap.put(troublePoint, ++latestTroublePointId);
        eventBus.post(new TroublePointCreatedEvent(latestTroublePointId, troublePoint));
        logger.info("Got message about trouble - CONSTRUCTION");
        logger.info("troublePoint: " + troublePoint.getLat() + "  " + troublePoint.getLng());
        if (!mapOfConstructionSiteBlockedEdges.containsKey(edgeId)) {
            mapOfConstructionSiteBlockedEdges.put(edgeId, rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM));
            ExtendedGraphHopper.addForbiddenEdges(Arrays.asList(edgeId));
        }
        sendBroadcast(generateMessageAboutTrouble(rcv, MessageParameter.CONSTRUCTION, MessageParameter.SHOW));
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
        else {
            mapOfLightTrafficJamBlockedEdges.replace(edgeId, rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM));
        }
        sendBroadcast(generateMessageAboutTrouble(rcv, MessageParameter.TRAFFIC_JAMS, MessageParameter.SHOW));
    }

    private void trafficJamsDisappearedHandle(ACLMessage rcv) {
        //TODO: Rysowanie w LightManager - Przemek
        Position positionOfTroubleLight = Position.of(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT),
                rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON));
        int edgeId = Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
        logger.info("Got message about light traffic jam stop on: " + edgeId);
        if (mapOfLightTrafficJamBlockedEdges.containsKey(edgeId)) {
            mapOfLightTrafficJamBlockedEdges.remove(edgeId);
            ExtendedGraphHopper.removeForbiddenEdges(Arrays.asList(edgeId));
        }
        sendBroadcast(generateMessageAboutTrouble(rcv, MessageParameter.TRAFFIC_JAMS, MessageParameter.STOP));
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
                                }
                                else if (rcv.getUserDefinedParameter(MessageParameter.TYPEOFTROUBLE).equals(MessageParameter.TRAFFIC_JAMS)) {
                                    trafficJamsAppearedHandle(rcv);
                                }
                            }
                            else if (rcv.getUserDefinedParameter(MessageParameter.TROUBLE).equals(MessageParameter.STOP)) {
                                if (rcv.getUserDefinedParameter(MessageParameter.TYPEOFTROUBLE).equals(MessageParameter.CONSTRUCTION)) {
                                    constructionHideHandle(rcv);
                                }
                                else {
                                    trafficJamsDisappearedHandle(rcv);
                                }
                            }
                        }
                    }
                }
                block(100);
            }

            private void constructionHideHandle(ACLMessage rcv) {
                var troublePoint = Position.of(Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT)),
                        Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON)));
                var id = troublePointsMap.remove(troublePoint);

                eventBus.post(new TroublePointVanishedEvent(id));
                logger.info("Hiding construction" + id);
            }

        };
        addBehaviour(communication);


        Behaviour sayAboutJam = new TickerBehaviour(this, 20_000 / TimeProvider.TIME_SCALE) {
            @Override
            protected void onTick() {
                if (configContainer.getSimulationState() != SimulationState.RUNNING) {
                    return;
                }

                if (!configContainer.shouldGenerateTrafficJams()) {
                    stop();
                }

                for (Map.Entry<Integer, String> entry : mapOfLightTrafficJamBlockedEdges.entrySet()) {
                    sendBroadcast(generateMessageAboutTrafficJam(entry.getKey(), entry.getValue(),
                            MessageParameter.TRAFFIC_JAMS, MessageParameter.SHOW));
                }
            }
        };
        addBehaviour(sayAboutJam);

    }
}
