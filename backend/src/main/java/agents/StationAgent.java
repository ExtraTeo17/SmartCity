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
import smartcity.ITimeProvider;
import smartcity.lights.OptimizationResult;
import smartcity.stations.StationStrategy;

import java.util.List;

public class StationAgent extends AbstractAgent {
    public static final String name = StationAgent.class.getSimpleName().replace("Agent", "");

    private final OSMStation station;
    private final StationStrategy stationStrategy;

    StationAgent(int id,
                 ITimeProvider timeProvider,
                 OSMStation station) { // REMEMBER TO PRUNE BEYOND CIRCLE
        super(id, name, timeProvider);

        this.station = station;
        this.stationStrategy = new StationStrategy(station, id);

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


            private void handleMessageFromBus(ACLMessage rcv) {
                var messageKind = rcv.getPerformative();
                String agentName = rcv.getSender().getLocalName();
                if (messageKind == ACLMessage.INFORM) {
                    String busLine = rcv.getUserDefinedParameter(MessageParameter.BUS_LINE);
                    stationStrategy.addBusAgentWithLine(agentName, busLine);

                    var scheduled = getDateParameter(rcv, MessageParameter.SCHEDULE_ARRIVAL);
                    var actual = getDateParameter(rcv, MessageParameter.ARRIVAL_TIME);
                    stationStrategy.addBusToFarAwayQueue(agentName, scheduled, actual);

                    print("Got INFORM from " + agentName);
                    // TODO: SEND MESSAGE ABOUT PASSENGERS
                }
                else if (messageKind == ACLMessage.REQUEST_WHEN) {
                    stationStrategy.removeBusFromFarAwayQueue(agentName);

                    var scheduled = getDateParameter(rcv, MessageParameter.SCHEDULE_ARRIVAL);
                    var actual = getDateParameter(rcv, MessageParameter.ARRIVAL_TIME);
                    stationStrategy.addBusToQueue(agentName, scheduled, actual);

                    var msg = createMessage(ACLMessage.AGREE, rcv.getSender());
                    print("Got REQUEST_WHEN from " + agentName);
                    send(msg);
                }
                else if (messageKind == ACLMessage.AGREE) {
                    stationStrategy.removeBusFromQueue(agentName);
                    print("Got AGREE from " + agentName);

                }
                else {
                    print("Unknown message type from " + agentName + ", type: " + messageKind, LoggerLevel.WARN);
                }
            }

            private void handleMessageFromPedestrian(ACLMessage rcv) {
                var messageKind = rcv.getPerformative();
                var agentName = rcv.getSender().getLocalName();
                if (messageKind == ACLMessage.INFORM) {
                    stationStrategy.addPedestrianToFarAwayQueue(agentName,
                            rcv.getUserDefinedParameter(MessageParameter.DESIRED_BUS_LINE),
                            getDateParameter(rcv, MessageParameter.ARRIVAL_TIME));
                }
                else if (messageKind == ACLMessage.REQUEST_WHEN) {
                    print("GET MESSAGE FROM PEDESTRIAN REQUEST_WHEN", LoggerLevel.DEBUG);
                    stationStrategy.removePedestrianFromFarAwayQueue(agentName,
                            rcv.getUserDefinedParameter(MessageParameter.DESIRED_BUS_LINE));
                    stationStrategy.addPedestrianToQueue(agentName,
                            rcv.getUserDefinedParameter(MessageParameter.DESIRED_BUS_LINE),
                            getDateParameter(rcv, MessageParameter.ARRIVAL_TIME));

                    var msg = createMessage(ACLMessage.REQUEST, rcv.getSender());
                    send(msg);
                }
                else if (messageKind == ACLMessage.AGREE) {
                    print("-----GET AGREE from PEDESTRIAN------", LoggerLevel.DEBUG);

                    var desiredBusLine = rcv.getUserDefinedParameter(MessageParameter.DESIRED_BUS_LINE);
                    if (!stationStrategy.removePedestrianFromQueue(agentName, desiredBusLine)) {
                        stationStrategy.removePedestrianFromFarAwayQueue(agentName, desiredBusLine);
                    }
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
                    var properties = createProperties(MessageParameter.STATION);
                    properties.setProperty(MessageParameter.BUS_AGENT_NAME, busAgentName);
                    msg.setAllUserDefinedParameters(properties);
                    send(msg);
                }

            }

            private void answerBusCanProceed(String busAgentName) {
                ACLMessage msg = createMessage(ACLMessage.REQUEST, busAgentName);
                Properties properties = createProperties(MessageParameter.STATION);
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