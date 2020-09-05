package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.elements.OSMStation;
import smartcity.lights.OptimizationResult;
import smartcity.stations.StationStrategy;

import java.time.Instant;
import java.util.List;

public class StationAgent extends AbstractAgent {
    public static final String name = StationAgent.class.getName().replace("Agent", "");
    private static final Logger logger = LoggerFactory.getLogger(StationAgent.class);

    private final OSMStation station;
    private final StationStrategy stationStrategy;

    @Override
    public String getNamePrefix() {
        return name;
    }

    StationAgent(final int agentId, OSMStation station) { // REMEMBER TO PRUNE BEYOND CIRCLE
        super(agentId);
        this.station = station;
        this.stationStrategy = new StationStrategy(station, agentId);

        Behaviour communication = new CyclicBehaviour() {
            @Override
            public void action() {

                ACLMessage rcv = receive();
                if (rcv != null) {
                    String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                    if (type.equals(MessageParameter.BUS)) {
                        handleMessageFromBus(rcv);
                    }
                    else if (type.equals(MessageParameter.PEDESTRIAN)) {
                        handleMessageFromPedestrian(rcv);

                    }
                }
                block(100);
            }

            private Instant getInstantParameter(ACLMessage rcv, String param) {
                return Instant.parse(rcv.getUserDefinedParameter(param));
            }

            private void handleMessageFromBus(ACLMessage rcv) {
                var messageKind = rcv.getPerformative();
                if (messageKind == ACLMessage.INFORM) {
                    String agentBusName = rcv.getSender().getLocalName();
                    String busLine = rcv.getUserDefinedParameter(MessageParameter.BUS_LINE);
                    stationStrategy.addBusToFarAwayQueue(agentBusName,
                            getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME),
                            getInstantParameter(rcv, MessageParameter.SCHEDULE_ARRIVAL));
                    stationStrategy.addMappingOfBusAndTheirAgent(agentBusName, busLine);
                    print("Got INFORM from " + rcv.getSender().getLocalName());
                    // TODO: SEND MESSAGE ABOUT PASSENGERS
                }
                else if (messageKind == ACLMessage.REQUEST_WHEN) {
                    stationStrategy.removeBusFromFarAwayQueue(rcv.getSender().getLocalName());
                    stationStrategy.addBusToQueue(rcv.getSender().getLocalName(),
                            getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME),
                            getInstantParameter(rcv, MessageParameter.SCHEDULE_ARRIVAL));
                    ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
                    msg.addReceiver(rcv.getSender());


                    print("Got REQUEST_WHEN from " + rcv.getSender().getLocalName());
                    send(msg);
                }
                else if (messageKind == ACLMessage.AGREE) {
                    stationStrategy.removeBusFromBusOnStationQueue(rcv.getSender().getLocalName());
                    print("Got AGREE from " + rcv.getSender().getLocalName());

                }
                else {
                    logger.warn("Unknown message type from Bus: " + messageKind);
                }
            }

            private void handleMessageFromPedestrian(ACLMessage rcv) {
                var messageKind = rcv.getPerformative();
                if (messageKind == ACLMessage.INFORM) {
                    stationStrategy.addPedestrianToFarAwayQueue(rcv.getSender().getLocalName(),
                            rcv.getUserDefinedParameter(MessageParameter.DESIRED_BUS),
                            getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME));
                }
                else if (messageKind == ACLMessage.REQUEST_WHEN) {
                    logger.debug("GET MESSAGE FROM PEDESTRIAN REQUEST_WHEN");
                    stationStrategy.removePedestrianFromFarAwayQueue(rcv.getSender().getLocalName(), rcv.getUserDefinedParameter(MessageParameter.DESIRED_BUS));
                    stationStrategy.addPedestrianToQueue(rcv.getSender().getLocalName(),
                            rcv.getUserDefinedParameter(MessageParameter.DESIRED_BUS),
                            getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME));
                    ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
                    msg.addReceiver(rcv.getSender());
                    send(msg);
                }
                else if (messageKind == ACLMessage.AGREE) {
                    logger.debug("-----GET AGREE from PEDESTRIAN------");
                    stationStrategy.removePedestrianFromBusOnStationQueue(rcv.getSender().getLocalName(), rcv.getUserDefinedParameter(MessageParameter.DESIRED_BUS));
                }
                else {
                    logger.warn("Unknown message type from Pedestrian: " + messageKind);
                }
            }
        };
        Behaviour checkState = new TickerBehaviour(this, 100) {
            @Override
            protected void onTick() {
                OptimizationResult result = stationStrategy.requestBusesAndPeopleFreeToGo();

                handleOptimizationResult(result);
            }

            private void handleOptimizationResult(OptimizationResult result) {
                List<Pair<String, List<String>>> elementsFreeToProceed = result.busesAndPedestriansFreeToProceed();
                for (Pair<String, List<String>> busAndPedestrians : elementsFreeToProceed) {
                    answerBusCanProceed(busAndPedestrians.getValue0());
                    answerPedestriansCanProceed(busAndPedestrians.getValue0(), busAndPedestrians.getValue1());
                }
            }

            private void answerPedestriansCanProceed(String busAgentName, List<String> pedestriansAgentsNames) {
                for (String name : pedestriansAgentsNames) {
                    ACLMessage msg = createMessage(ACLMessage.REQUEST, name);
                    Properties properties = new Properties();
                    properties.setProperty(MessageParameter.TYPE, MessageParameter.STATION);
                    properties.setProperty(MessageParameter.BUS_AGENT_NAME, busAgentName);
                    msg.setAllUserDefinedParameters(properties);
                    send(msg);
                }

            }

            private void answerBusCanProceed(String busAgentName) {
                ACLMessage msg = createMessage(ACLMessage.REQUEST, busAgentName);
                Properties properties = new Properties();
                properties.setProperty(MessageParameter.TYPE, MessageParameter.STATION);
                msg.setAllUserDefinedParameters(properties);
                send(msg);
            }
        };

        addBehaviour(communication);
        addBehaviour(checkState);
    }

    public OSMStation getStation() {
        return station;
    }
}