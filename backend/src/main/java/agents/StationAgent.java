package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.LoggerLevel;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.javatuples.Pair;
import osmproxy.elements.OSMStation;
import routing.nodes.StationNode;
import smartcity.ITimeProvider;
import smartcity.lights.OptimizationResult;
import smartcity.stations.StationStrategy;

import java.util.List;
import java.util.Map;

import static agents.AgentConstants.DEFAULT_BLOCK_ON_ERROR;
import static agents.message.MessageManager.createMessage;
import static agents.message.MessageManager.createProperties;
import static agents.utilities.BehaviourWrapper.wrapErrors;

/**
 * StationManager agent represents a bus stop. The main aim of it is to
 * manage arriving buses and passengers. He is informed about time of arrival of
 * approaching buses, as well as pedestrians. Thanks to that information
 * StationManager can apply strategy and decide whether particular bus
 * should wait for arriving passengers.
 */
public class StationAgent extends AbstractAgent {
    public static final String name = StationAgent.class.getSimpleName().replace("Agent", "");

    private final OSMStation station;

    StationAgent(int id,
                 OSMStation station,
                 StationStrategy stationStrategy,
                 ITimeProvider timeProvider,
                 EventBus eventBus) { // REMEMBER TO PRUNE BEYOND CIRCLE
        super(id, name, timeProvider, eventBus);
        this.station = station;

        Behaviour communication = new CyclicBehaviour() {
            @SuppressWarnings("DuplicatedCode")
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

                switch (type) {
                    case MessageParameter.BUS -> handleMessageFromBus(rcv);
                    case MessageParameter.PEDESTRIAN -> handleMessageFromPedestrian(rcv);
                }
            }


            private void handleMessageFromBus(ACLMessage rcv) {
                var messageKind = rcv.getPerformative();
                String agentName = rcv.getSender().getLocalName();
                if (messageKind == ACLMessage.INFORM) {
                    if(rcv.getUserDefinedParameter(MessageParameter.TYPEOFTROUBLE)!=null &&
                            rcv.getUserDefinedParameter(MessageParameter.TYPEOFTROUBLE).equals(MessageParameter.CRASH)) {

                        handleCrashFromBusToConcernedPassengers(rcv);

                    }
                    else {
                        print("Got INFORM from " + agentName);
                        String busLine = rcv.getUserDefinedParameter(MessageParameter.BUS_LINE);
                        stationStrategy.addBusAgentWithLine(agentName, busLine);

                        var scheduled = getDateParameter(rcv, MessageParameter.SCHEDULE_ARRIVAL);
                        var actual = getDateParameter(rcv, MessageParameter.ARRIVAL_TIME);
                        stationStrategy.addBusToFarAwayQueue(agentName, scheduled, actual);

                        // TODO: SEND MESSAGE ABOUT PASSENGERS
                    }
                }
                else if (messageKind == ACLMessage.REQUEST_WHEN) {
                    print("Got REQUEST_WHEN from " + agentName);
                    stationStrategy.removeBusFromFarAwayQueue(agentName);

                    var scheduled = getDateParameter(rcv, MessageParameter.SCHEDULE_ARRIVAL);
                    var actual = getDateParameter(rcv, MessageParameter.ARRIVAL_TIME);
                    stationStrategy.addBusToQueue(agentName, scheduled, actual);

                    var msg = createMessage(ACLMessage.AGREE, rcv.getSender());
                    // TODO: This is send to busAgent without type - won't be handled
                    //  I am not sure if needed
                    logger.info("SEND AGREE in answer to REQUEST WHEN");
                    msg.setAllUserDefinedParameters(createProperties(MessageParameter.STATION));
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

            private void handleCrashFromBusToConcernedPassengers(ACLMessage rcv) {
                String crashedLine = rcv.getUserDefinedParameter(MessageParameter.BUS_LINE);
                ACLMessage responseToCrash = createResponseToCrash(rcv);


                    for (Map.Entry<String, String> entry : station.getAgentMap().entrySet()) {
                        if (entry.getValue().equals(crashedLine)) {
                            responseToCrash.addReceiver(new AID(entry.getKey(),AID.ISLOCALNAME));
                        }
                    }
                    send(responseToCrash);
            }

            private ACLMessage createResponseToCrash(ACLMessage rcv) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID(name, AID.ISLOCALNAME));

                Properties properties = createProperties(MessageParameter.STATION);
                properties.setProperty(MessageParameter.TROUBLE, MessageParameter.SHOW);
                properties.setProperty(MessageParameter.TYPEOFTROUBLE, MessageParameter.CRASH);
                properties.setProperty(MessageParameter.TROUBLE, MessageParameter.SHOW);
                properties.setProperty(MessageParameter.TROUBLE_LAT, station.getLat()+"");
                properties.setProperty(MessageParameter.TROUBLE_LON,station.getLng()+"");
                properties.setProperty(MessageParameter.DESIRED_OSM_STATION_ID, station.getId() + "");
                properties.setProperty(MessageParameter.AGENT_ID_OF_NEXT_CLOSEST_STATION, getId()+"" );
                    //maybe not needed
                properties.setProperty(MessageParameter.LAT_OF_NEXT_CLOSEST_STATION,station.getLat()+ "");
                properties.setProperty(MessageParameter.LON_OF_NEXT_CLOSEST_STATION, station.getLng() + "");

                properties.setProperty(MessageParameter.BUS_LINE,rcv.getUserDefinedParameter(MessageParameter.BUS_LINE));
                properties.setProperty(MessageParameter.BRIGADE,rcv.getUserDefinedParameter(MessageParameter.BRIGADE));

                msg.setAllUserDefinedParameters(properties);
                return msg;
            }



            private void handleMessageFromPedestrian(ACLMessage rcv) {
                var messageKind = rcv.getPerformative();
                var agentName = rcv.getSender().getLocalName();
                if (messageKind == ACLMessage.INFORM) {
                    print("Got INFORM from " + agentName);
                    String desiredBusLine = rcv.getUserDefinedParameter(MessageParameter.BUS_LINE);
                    station.addToAgentMap(agentName, desiredBusLine);
                    stationStrategy.addPedestrianToFarAwayQueue(agentName, desiredBusLine,
                            getDateParameter(rcv, MessageParameter.ARRIVAL_TIME));
                }
                else if (messageKind == ACLMessage.REQUEST_WHEN) {
                    print("Got REQUEST_WHEN from " + agentName);
                    var desiredBusLine = station.getFromAgentMap(agentName);
                    stationStrategy.removePedestrianFromFarAwayQueue(agentName, desiredBusLine);
                    stationStrategy.addPedestrianToQueue(agentName, desiredBusLine,
                            getDateParameter(rcv, MessageParameter.ARRIVAL_TIME));

                    stationStrategy.removeFromToWhomWaitMap(agentName, desiredBusLine);


                    //var msg = createMessage(ACLMessage.REQUEST, rcv.getSender());
                    // TODO: This is send to pedestrian without type - won't be handled
                    //  But also needs busAgent name parameter or Pedestrian will die
                    //  I am not sure if needed
                    // var properties = createProperties(MessageParameter.STATION);
                    // msg.setAllUserDefinedParameters(createProperties(MessageParameter.STATION));
                    //send(msg);
                }
                else if (messageKind == ACLMessage.AGREE) {
                    print("-----GET AGREE from PEDESTRIAN------", LoggerLevel.DEBUG);

                    var desiredBusLine = station.getFromAgentMap(agentName);
                    station.removeFromAgentMap(agentName);
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
                    logger.debug("Send REQUEST to " + name);
                    ACLMessage msg = createMessage(ACLMessage.REQUEST, name);
                    var properties = createProperties(MessageParameter.STATION);
                    properties.setProperty(MessageParameter.BUS_AGENT_NAME, busAgentName);
                    msg.setAllUserDefinedParameters(properties);
                    send(msg);
                }
            }

            private void answerBusCanProceed(String busAgentName) {
                logger.debug("Send REQUEST to " + busAgentName);
                ACLMessage msg = createMessage(ACLMessage.REQUEST, busAgentName);
                Properties properties = createProperties(MessageParameter.STATION);
                msg.setAllUserDefinedParameters(properties);
                send(msg);
            }
        };

        var onError = createErrorConsumer();
        addBehaviour(wrapErrors(communication, onError));
        addBehaviour(wrapErrors(checkState, onError));
    }

    public OSMStation getStation() {
        return station;
    }
}