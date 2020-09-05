package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.LightManagerNode;
import routing.Router;
import routing.StationNode;
import smartcity.MasterAgent;
import vehicles.DrivingState;
import vehicles.Pedestrian;

import java.time.Instant;

public class PedestrianAgent extends AbstractAgent {
    public static final String name = StationAgent.class.getName().replace("Agent", "");
    private static final Logger logger = LoggerFactory.getLogger(PedestrianAgent.class);
    private final Pedestrian pedestrian;

    @Override
    public String getNamePrefix() {
        return name;
    }

    public PedestrianAgent(int agentId, final Pedestrian pedestrian) {
        super(agentId);
        this.pedestrian = pedestrian;
    }

    public boolean isInBus() { return DrivingState.IN_BUS == pedestrian.getState();}

    @Override
    protected void setup() {
        getNextStation();

        pedestrian.setState(DrivingState.MOVING);
        Behaviour move = new TickerBehaviour(this, Router.STEP_CONSTANT / pedestrian.getSpeed()) {
            @Override
            public void onTick() {
                if (pedestrian.isAtTrafficLights()) {
                    switch (pedestrian.getState()) {
                        case MOVING:
                            LightManagerNode light = pedestrian.getCurrentTrafficLightNode();
                            ACLMessage msg = createMessage(ACLMessage.REQUEST_WHEN, LightManagerAgent.name,
                                    light.getLightManagerId());
                            Properties properties = new Properties();
                            properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(pedestrian.getAdjacentOsmWayId()));
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                            pedestrian.setState(DrivingState.WAITING_AT_LIGHT);
                            print("Asking LightManager" + light.getLightManagerId() + " for right to passage.");
                            break;

                        case WAITING_AT_LIGHT:

                            break;
                        case PASSING_LIGHT:
                            print("Passing the light.");
                            pedestrian.move();
                            pedestrian.setState(DrivingState.MOVING);
                            break;
                    }
                }
                else if (pedestrian.isAtStation()) {
                    switch (pedestrian.getState()) {
                        case MOVING:
                            StationNode station = pedestrian.getStartingStation();
                            ACLMessage msg = createMessage(ACLMessage.REQUEST_WHEN, StationAgent.name, station.getStationId());

                            Properties properties = new Properties();
                            properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.DESIRED_BUS, "" + pedestrian.getPreferredBusLine());
                            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + MasterAgent.getSimulationTime().toInstant());
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                            print("Send REQUEST_WHEN to Station");

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
                    print("Reached destination.");

                    ACLMessage msg = createMessage(ACLMessage.INFORM, MasterAgent.name);
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
                if (rcv == null) {
                    return;
                }

                String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                if (type == null) {
                    return;
                }

                switch (type) {
                    case MessageParameter.LIGHT:
                        switch (rcv.getPerformative()) {
                            case ACLMessage.REQUEST:
                                if (pedestrian.getState() == DrivingState.WAITING_AT_LIGHT) {
                                    ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());

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
                                ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());

                                Properties properties = new Properties();
                                properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
                                properties.setProperty(MessageParameter.DESIRED_BUS, pedestrian.getPreferredBusLine());
                                response.setAllUserDefinedParameters(properties);
                                send(response);

                                ACLMessage msg = createMessage(ACLMessage.REQUEST_WHEN,
                                        rcv.getUserDefinedParameter(MessageParameter.BUS_AGENT_NAME));
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
                                ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());

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
            ACLMessage msg = createMessage(ACLMessage.INFORM, StationAgent.name, nextStation.getStationId());

            Properties properties = new Properties();
            Instant currentTime = MasterAgent.getSimulationTime().toInstant();
            Instant time = currentTime.plusMillis(pedestrian.getMillisecondsToNextStation());
            properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + time);
            properties.setProperty(MessageParameter.DESIRED_BUS, "" + pedestrian.getPreferredBusLine());
            msg.setAllUserDefinedParameters(properties);

            send(msg);
            print("Send INFORM to Station");
        }
    }

    public Pedestrian getPedestrian() {
        return pedestrian;
    }
}
