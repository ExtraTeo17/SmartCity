package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import smartcity.ITimeProvider;
import smartcity.config.ConfigContainer;
import smartcity.lights.OptimizationResult;
import smartcity.lights.abstractions.ICrossroad;
import smartcity.lights.core.Light;
import smartcity.stations.ArrivalInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static agents.AgentConstants.DEFAULT_BLOCK_ON_ERROR;
import static agents.message.MessageManager.*;

/**
 * The main aim of LightManager agent is to manage a specific group of
 * lights and control traffic. Moreover, LightManager collects
 * information from cars/pedestrians/bikes about the time they arrive, and
 * receives requests to pass from the cars/pedestrians/bikes.
 * LightManager decides when a particular light should switch color,
 * which depends on a chosen strategy.
 */
public class LightManagerAgent extends AbstractAgent {
    public static final String name = LightManagerAgent.class.getSimpleName().replace("Agent", "");
    private final ICrossroad crossroad;
    private final Map<String, String> trafficJammedEdgeSet = new HashMap<>();
    private final ConfigContainer configContainer;

    LightManagerAgent(int id, ICrossroad crossroad,
                      ITimeProvider timeProvider,
                      EventBus eventBus,
                      ConfigContainer configContainer) {
        super(id, name, timeProvider, eventBus);
        this.crossroad = crossroad;
        this.configContainer = configContainer;
    }

    @Override
    protected void setup() {
        print("I'm a traffic manager.");
        crossroad.startLifetime();

        var notifyCarAboutGreen = new TickerBehaviour(this, 10_000 / timeProvider.getTimeScale()) {
            @Override
            protected void onTick() {
                //for all Light check
                //check if time from last green > written time
                // if so, put in the queue
                //if not
                // check count of people (remember about 2 person on pedestrian light= 1 car)
                // if queue is empty
                // apply strategy
                //for elements in queue (if there are elements in queue, make green)
                OptimizationResult result = crossroad.requestOptimizations(configContainer.getExtendLightTime());
                try {
                    handleOptimizationResult(result);
                } catch (Exception e) {
                    logger.warn("Error handling optimization result", e);
                }
            }

            private void handleOptimizationResult(OptimizationResult result) {
                //Expected one agent in the list
                final List<String> agentsFreeToProceed = result.carsFreeToProceed();

                final String agentStuckInJam = result.getAgentStuckInJam();
                if (result.shouldNotifyCarAboutStartOfTrafficJamOnThisLight()) {
                    logger.info("KOREK");
                    handleTrafficJams(result, agentStuckInJam);
                }
                if (result.shouldNotifyCarAboutStopOfTrafficJamOnThisLight()) {
                    sendMessageAboutTroubleStopToTroubleManager(result);
                }

                if (agentsFreeToProceed.size() != 0) {
                    for (String agentName : agentsFreeToProceed) {
                        answerCanProceed(agentName);
                    }
                }
            }

            private synchronized void sendMessageAboutTroubleStopToTroubleManager(OptimizationResult result) {
                // Remember that this solution is based on different agents expected to return the
                //  same graphhopper edge ID when traffic jam starts and stops
                ACLMessage msg = createMessage(ACLMessage.INFORM, TroubleManagerAgent.name);
                Properties properties = createProperties(MessageParameter.LIGHT);
                properties.setProperty(MessageParameter.TYPEOFTROUBLE, MessageParameter.TRAFFIC_JAM);
                properties.setProperty(MessageParameter.TROUBLE, MessageParameter.STOP);
                var jammedLight = result.getJammedLightPosition();
                properties.setProperty(MessageParameter.TROUBLE_LAT, Double.toString(jammedLight.getLat()));
                properties.setProperty(MessageParameter.TROUBLE_LON, Double.toString(jammedLight.getLng()));
                properties.setProperty(MessageParameter.EDGE_ID, trafficJammedEdgeSet.get(
                        String.valueOf(result.getOsmWayId())));
                trafficJammedEdgeSet.remove(Long.toString(result.getOsmWayId()));
                msg.setAllUserDefinedParameters(properties);
                logger.debug("Send message to " + TroubleManagerAgent.name + " for request of EdgeID when stopping traffic jam");
                send(msg);
            }

            private void handleTrafficJams(OptimizationResult result, String nameOfAgent) {
                // TODO shouldNotifyCarAboutTrafficJamOnThisLight have an old state to stop the jam
                // TODO: use result.getJammedLight(...)
                if (configContainer.isTrafficJamStrategyActive()) {
                    sendMessageAboutTroubleToVehicle(result, nameOfAgent);
                }
                // TODO FOR PRZEMEK: add eventBus.startTrafficJamEvent here IN THIS LINE
                // and delete in TroubleManager (the pulsowanie should start here, not
                // after the message goes through car to TroubleManager
                // You have the coordinates in result.getJammedLightPosition()
            }

            private void sendMessageAboutTroubleToVehicle(OptimizationResult result, String nameOfAgent) {
                // Remember that this solution is based on different agents expected
                //  to return the same graphhopper edge ID when traffic jam starts and stops
                ACLMessage msg = createMessage(ACLMessage.PROPOSE, nameOfAgent);
                Properties properties = createProperties(MessageParameter.LIGHT);
                properties.setProperty(MessageParameter.TYPEOFTROUBLE, MessageParameter.TRAFFIC_JAM);
                properties.setProperty(MessageParameter.TROUBLE, MessageParameter.SHOW);
                properties.setProperty(MessageParameter.LENGTH_OF_JAM, Double.toString(result.getLengthOfJam()));
                properties.setProperty(MessageParameter.TROUBLE_LAT, Double.toString(result.getJammedLightPosition().getLat()));
                properties.setProperty(MessageParameter.TROUBLE_LON, Double.toString(result.getJammedLightPosition().getLng()));
                properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(result.getOsmWayId()));
                msg.setAllUserDefinedParameters(properties);
                logger.debug("Send message to " + nameOfAgent + " for request of EdgeID when starting traffic jam");
                send(msg);
            }

            private void answerCanProceed(String carName) {
                logger.debug("Send request to proceed to " + carName);
                ACLMessage msg = createMessage(ACLMessage.REQUEST, carName);
                Properties properties = createProperties(MessageParameter.LIGHT);
                msg.setAllUserDefinedParameters(properties);
                send(msg);
            }
        };

        var communicate = new CyclicBehaviour() {

            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv == null) {
                    block();
                    return;
                }

                String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                if (type == null) {
                    block(DEFAULT_BLOCK_ON_ERROR);
                    logTypeError(rcv);
                    return;
                }

                handleMessageByType(rcv, type);
            }

            private void handleMessageByType(ACLMessage rcv, String type) {
                switch (type) {
                    case MessageParameter.VEHICLE, MessageParameter.BIKE -> handleMessageFromVehicle(rcv);
                    case MessageParameter.PEDESTRIAN -> handleMessageFromPedestrian(rcv);
                }
            }

            private void handleMessageFromVehicle(ACLMessage rcv) {
                // TODO: Should be refactored - too much usage of crossroad methods.
                var agentName = getSender(rcv);
                switch (rcv.getPerformative()) {
                    case ACLMessage.INFORM -> {
                        var time = getDateParameter(rcv, MessageParameter.ARRIVAL_TIME);
                        var arrivalInfo = ArrivalInfo.of(agentName, time);
                        logger.debug("Add " + agentName + " to far away queue");
                        crossroad.addCarToFarAwayQueue(getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID), arrivalInfo);
                    }
                    case ACLMessage.REQUEST_WHEN -> {
                        crossroad.removeCarFromFarAwayQueue(getIntParameter(rcv,
                                MessageParameter.ADJACENT_OSM_WAY_ID), agentName);
                        ACLMessage agree = createMessage(ACLMessage.AGREE, agentName);
                        Properties properties = createProperties(MessageParameter.LIGHT);
                        agree.setAllUserDefinedParameters(properties);
                        send(agree);
                        logger.debug("Add " + agentName + " to close queue");
                        crossroad.addCarToQueue(getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID), agentName);
                    }
                    case ACLMessage.AGREE -> {
                        logger.debug("Remove " + agentName + " from close queue");
                        crossroad.removeCarFromQueue(getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID));
                    }
                    case ACLMessage.REFUSE -> {
                        logger.debug("Remove " + agentName + " from far away queue");
                        crossroad.removeCarFromFarAwayQueue(getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID), agentName);
                    }
                    case ACLMessage.CONFIRM -> {
                        logger.debug("Add edge " + rcv.getUserDefinedParameter(MessageParameter.EDGE_ID) + " from " + agentName + " to jammed edge set");
                        String wayIdOsm = rcv.getUserDefinedParameter(MessageParameter.ADJACENT_OSM_WAY_ID);
                        trafficJammedEdgeSet.put(wayIdOsm, rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
                        crossroad.getLights().stream().filter(el -> el.getAdjacentWayId() == Long.parseLong(wayIdOsm))
                                .findFirst().ifPresent(el -> el.setTrafficJamOngoing(true));
                    }
                    default -> logger.debug("Wait");
                }
            }

            private void handleMessageFromPedestrian(ACLMessage rcv) {
                // TODO: Should be refactored - too much usage of crossroad methods.
                var agentName = getSender(rcv);
                switch (rcv.getPerformative()) {
                    case ACLMessage.INFORM -> {
                        var time = getDateParameter(rcv, MessageParameter.ARRIVAL_TIME);
                        var arrivalInfo = ArrivalInfo.of(agentName, time);
                        crossroad.addPedestrianToFarAwayQueue(getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID),
                                arrivalInfo);
                        logger.debug("Got inform from pedestrian" + rcv.getSender().getLocalName());
                    }
                    case ACLMessage.REQUEST_WHEN -> {
                        crossroad.removePedestrianFromFarAwayQueue(getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID),
                                agentName);
                        ACLMessage agree = createMessage(ACLMessage.AGREE, rcv.getSender());
                        Properties properties = createProperties(MessageParameter.LIGHT);
                        agree.setAllUserDefinedParameters(properties);
                        send(agree);
                        crossroad.addPedestrianToQueue(getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID), agentName);
                        logger.debug("Got request_when from pedestrian" + rcv.getSender().getLocalName());
                    }
                    case ACLMessage.AGREE -> {
                        logger.debug("Got agree from pedestrian" + rcv.getSender().getLocalName());
                        crossroad.removePedestrianFromQueue(getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID));
                    }
                    default -> print("Wait");
                }
            }
        };

        addBehaviour(notifyCarAboutGreen);
        addBehaviour(communicate);
    }

    public List<Light> getLights() {
        return crossroad.getLights();
    }
}
