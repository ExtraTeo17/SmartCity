package agents;

import agents.utils.MessageParameter;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.LightManagerNode;
import routing.StationNode;
import smartcity.MasterAgent;
import vehicles.DrivingState;
import vehicles.Pedestrian;

import java.time.Instant;

public class PedestrianAgent extends AbstractAgent {
    private static final Logger logger = LoggerFactory.getLogger(PedestrianAgent.class);
    private final Pedestrian pedestrian;

    @Override
    public String getNamePrefix() {
        return "Pedestrian";
    }

    public PedestrianAgent(final Pedestrian pedestrian, final int agentId) {
        super(agentId);
        this.pedestrian = pedestrian;
    }

    public boolean isInBus() { return DrivingState.IN_BUS == pedestrian.getState();}

    @Override
    protected void setup() {
        getNextStation();

        pedestrian.setState(DrivingState.MOVING);
        Behaviour move = new TickerBehaviour(this, 3600 / pedestrian.getSpeed()) {
            @Override
            public void onTick() {
                if (pedestrian.isAtTrafficLights()) {
                    switch (pedestrian.getState()) {
                        case MOVING:
                            LightManagerNode light = pedestrian.getCurrentTrafficLightNode();
                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
                            msg.addReceiver(new AID("LightManager" + light.getLightManagerId(), AID.ISLOCALNAME));
                            Properties properties = new Properties();
                            properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(pedestrian.getAdjacentOsmWayId()));
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                            pedestrian.setState(DrivingState.WAITING_AT_LIGHT);
                            Print("Asking LightManager" + light.getLightManagerId() + " for right to passage.");
                            break;

                        case WAITING_AT_LIGHT:

                            break;
                        case PASSING_LIGHT:
                            Print("Passing the light.");
                            pedestrian.move();
                            pedestrian.setState(DrivingState.MOVING);
                            break;
                    }
                }
                else if (pedestrian.isAtStation()) {
                    switch (pedestrian.getState()) {
                        case MOVING:
                            StationNode station = pedestrian.getStartingStation();

                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
                            msg.addReceiver(new AID("Station" + station.getStationId(), AID.ISLOCALNAME));
                            Properties properties = new Properties();
                            properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.DESIRED_BUS, "" + pedestrian.getPreferredBusLine());
                            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + MasterAgent.getSimulationTime().toInstant());
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                            logger.info("Pedestrian: Send REQUEST_WHEN to Station");

                            pedestrian.setState(DrivingState.WAITING_AT_STATION);
                            break;
                        case WAITING_AT_STATION:
                            // waiting for bus...

                            break;
                        case IN_BUS:
                            // traveling inside a bus

                            break;
                        case PASSING_STATION:

                            pedestrian.move();
                            pedestrian.setState(DrivingState.MOVING);
                            break;
                    }
                }
                else if (pedestrian.isAtDestination()) {
                    pedestrian.setState(DrivingState.AT_DESTINATION);
                    Print("Reached destination.");

                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID(MasterAgent.name, AID.ISLOCALNAME));
                    Properties prop = new Properties();
                    prop.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
                    prop.setProperty(MessageParameter.AT_DESTINATION, String.valueOf(Boolean.TRUE));
                    msg.setAllUserDefinedParameters(prop);
                    send(msg);
                    doDelete();
                }
                else {
                    pedestrian.move();
                }
            }
        };

        Behaviour communication = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv == null)
                    return;
                
                String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                if(type == null)
                    return;

                switch (type) {
                    case MessageParameter.LIGHT:
                        switch (rcv.getPerformative()) {
                            case ACLMessage.REQUEST:
                                if (pedestrian.getState() == DrivingState.WAITING_AT_LIGHT) {
                                    ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                                    response.addReceiver(rcv.getSender());
                                    Properties properties = new Properties();

                                    properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
                                    properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(pedestrian.getAdjacentOsmWayId()));
                                    response.setAllUserDefinedParameters(properties);
                                    send(response);
                                    if (pedestrian.findNextStop() instanceof LightManagerNode) {
                                        informLightManager(pedestrian);
                                    }
                                    pedestrian.setState(DrivingState.PASSING_LIGHT);
                                }
                                break;
                        }
                        break;
                    case MessageParameter.STATION:
                        switch (rcv.getPerformative()) {
                            case ACLMessage.REQUEST:
                                ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                                response.addReceiver(rcv.getSender());
                                Properties properties = new Properties();

                                properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
                                properties.setProperty(MessageParameter.DESIRED_BUS, pedestrian.getPreferredBusLine());
                                response.setAllUserDefinedParameters(properties);
                                send(response);

                                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
                                String busAgentId = rcv.getUserDefinedParameter(MessageParameter.BUS_ID);
                                msg.addReceiver(new AID(busAgentId, AID.ISLOCALNAME));
                                properties = new Properties();
                                properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
                                properties.setProperty(MessageParameter.STATION_ID, "" + pedestrian.getTargetStation().getStationId());
                                msg.setAllUserDefinedParameters(properties);
                                pedestrian.setState(DrivingState.IN_BUS);
                                send(msg);

                                while (!pedestrian.isAtStation()) {
                                    pedestrian.move();
                                }

                                break;
                        }
                        break;
                    case MessageParameter.BUS:
                        switch (rcv.getPerformative()) {
                            case ACLMessage.REQUEST:
                                ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                                response.addReceiver(rcv.getSender());
                                Properties properties = new Properties();

                                properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
                                properties.setProperty(MessageParameter.STATION_ID, "" + pedestrian.getTargetStation().getStationId());
                                response.setAllUserDefinedParameters(properties);
                                send(response);
                                pedestrian.move();
                                pedestrian.setState(DrivingState.PASSING_STATION);

                                informLightManager(pedestrian);

                                break;
                        }
                        break;
                }
            }
        };

        addBehaviour(move);
        addBehaviour(communication);
    }

    void getNextStation() {
        // finds next station and announces his arrival
        StationNode nextStation = pedestrian.findNextStation();
        pedestrian.setState(DrivingState.MOVING);
        if (nextStation != null) {
            logger.info("Pedestrian: send INFORM to station");
            AID dest = new AID("Station" + nextStation.getStationId(), AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(dest);
            Properties properties = new Properties();
            Instant currentTime = MasterAgent.getSimulationTime().toInstant();
            Instant time = currentTime.plusMillis(pedestrian.getMilisecondsToNextStation());
            properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + time);
            properties.setProperty(MessageParameter.DESIRED_BUS, "" + pedestrian.getPreferredBusLine());
            msg.setAllUserDefinedParameters(properties);

            send(msg);
            logger.info("Pedestrian: Send Inform to Station");
        }
    }

    public Pedestrian getPedestrian() {
        return pedestrian;
    }

    void Print(String message) {
        logger.info(getLocalName() + ": " + message);
    }
}
