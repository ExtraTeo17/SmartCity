package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.LoggerLevel;
import agents.utilities.MessageParameter;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.javatuples.Pair;
import osmproxy.elements.OSMStation;
import smartcity.MasterAgent;
import smartcity.lights.OptimizationResult;
import smartcity.stations.StationStrategy;

import java.time.Instant;
import java.util.List;

public class StationAgent extends AbstractAgent {
    public static final String name = StationAgent.class.getName().replace("Agent", "");

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
                var paramValue = rcv.getUserDefinedParameter(param);
                if (paramValue == null) {
                    print("Did not receive " + param + " from " + rcv.getSender(), LoggerLevel.ERROR);
                    return MasterAgent.getSimulationTime().toInstant();
                }

                return Instant.parse(paramValue);
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
                    print("Unknown message type from Bus: " + messageKind, LoggerLevel.WARN);
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
                    print("GET MESSAGE FROM PEDESTRIAN REQUEST_WHEN", LoggerLevel.DEBUG);
                    stationStrategy.removePedestrianFromFarAwayQueue(rcv.getSender().getLocalName(), rcv.getUserDefinedParameter(MessageParameter.DESIRED_BUS));
                    stationStrategy.addPedestrianToQueue(rcv.getSender().getLocalName(),
                            rcv.getUserDefinedParameter(MessageParameter.DESIRED_BUS),
                            getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME));
                    ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
                    msg.addReceiver(rcv.getSender());
                    send(msg);
                }
                else if (messageKind == ACLMessage.AGREE) {
                    print("-----GET AGREE from PEDESTRIAN------", LoggerLevel.DEBUG);
                    stationStrategy.removePedestrianFromBusOnStationQueue(rcv.getSender().getLocalName(), rcv.getUserDefinedParameter(MessageParameter.DESIRED_BUS));
                }
                else {
                    print("Unknown message type from Pedestrian: " + messageKind, LoggerLevel.WARN);
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