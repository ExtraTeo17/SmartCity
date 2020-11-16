package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import routing.LightManagerNode;
import routing.RoutingConstants;
import routing.StationNode;
import smartcity.ITimeProvider;
import smartcity.SmartCityAgent;
import vehicles.DrivingState;
import vehicles.Pedestrian;

public class PedestrianAgent extends AbstractAgent {
    public static final String name = PedestrianAgent.class.getSimpleName().replace("Agent", "");

    private final Pedestrian pedestrian;

    PedestrianAgent(int agentId,
                    Pedestrian pedestrian,
                    ITimeProvider timeProvider,
                    EventBus eventBus) {
        super(agentId, name, timeProvider, eventBus);
        this.pedestrian = pedestrian;
    }

    public boolean isInBus() { return DrivingState.IN_BUS == pedestrian.getState();}

    @Override
    protected void setup() {
        getNextStation();

        pedestrian.setState(DrivingState.MOVING);
        Behaviour move = new TickerBehaviour(this, RoutingConstants.STEP_CONSTANT / pedestrian.getSpeed()) {
            @Override
            public void onTick() {
                if (pedestrian.isAtTrafficLights()) {
                    switch (pedestrian.getState()) {
                        case MOVING:
                            LightManagerNode light = pedestrian.getCurrentTrafficLightNode();
                            ACLMessage msg = createMessage(ACLMessage.REQUEST_WHEN, LightManagerAgent.name,
                                    light.getLightManagerId());
                            Properties properties = createProperties(MessageParameter.PEDESTRIAN);
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
                            ACLMessage msg = createMessage(ACLMessage.REQUEST_WHEN, StationAgent.name, station.getAgentId());
                            Properties properties = createProperties(MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.DESIRED_BUS_LINE, pedestrian.getPreferredBusLine());
                            properties.setProperty(MessageParameter.ARRIVAL_TIME, timeProvider.getCurrentSimulationTime()
                                    .toString());
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

                    ACLMessage msg = createMessage(ACLMessage.INFORM, SmartCityAgent.name);
                    Properties prop = createProperties(MessageParameter.PEDESTRIAN);
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
                    logTypeError(rcv);
                    return;
                }

                switch (type) {
                    case MessageParameter.LIGHT:
                        if (rcv.getPerformative() == ACLMessage.REQUEST) {
                            if (pedestrian.getState() == DrivingState.WAITING_AT_LIGHT) {
                                ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());
                                Properties properties = createProperties(MessageParameter.PEDESTRIAN);
                                properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID,
                                        Long.toString(pedestrian.getAdjacentOsmWayId()));
                                response.setAllUserDefinedParameters(properties);
                                send(response);

                                if (pedestrian.findNextStop() instanceof LightManagerNode) {
                                    informLightManager(pedestrian);
                                }
                                pedestrian.setState(DrivingState.PASSING_LIGHT);
                            }
                        }
                        break;
                    case MessageParameter.STATION:
                        if (rcv.getPerformative() == ACLMessage.REQUEST) {
                            ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());
                            var properties = createProperties(MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.DESIRED_BUS_LINE, pedestrian.getPreferredBusLine());
                            response.setAllUserDefinedParameters(properties);
                            send(response);

                            ACLMessage msg = createMessage(ACLMessage.REQUEST_WHEN,
                                    rcv.getUserDefinedParameter(MessageParameter.BUS_AGENT_NAME));
                            properties = createProperties(MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.STATION_ID, String.valueOf(pedestrian.getTargetStation()
                                    .getAgentId()));
                            msg.setAllUserDefinedParameters(properties);
                            pedestrian.setState(DrivingState.IN_BUS);
                            send(msg);

                            // TODO: What's happening here? Why does he use teleportation?
                            while (!pedestrian.isAtStation() && !pedestrian.isAtDestination()) {
                                pedestrian.move();
                            }
                        }
                        break;
                    case MessageParameter.BUS:
                        if (rcv.getPerformative() == ACLMessage.REQUEST) {
                            ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());

                            Properties properties = createProperties(MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.STATION_ID, String.valueOf(pedestrian.getTargetStation()
                                    .getAgentId()));
                            response.setAllUserDefinedParameters(properties);
                            send(response);

                            if (!pedestrian.isAtDestination()) {
                                pedestrian.move();
                                pedestrian.setState(DrivingState.PASSING_STATION);
                            }
                            informLightManager(pedestrian);
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
            ACLMessage msg = createMessage(ACLMessage.INFORM, StationAgent.name, nextStation.getAgentId());
            Properties properties = createProperties(MessageParameter.PEDESTRIAN);
            var currentTime = timeProvider.getCurrentSimulationTime();
            var predictedTime = currentTime.plusNanos(pedestrian.getMillisecondsToNextStation() * 1_000_000);
            properties.setProperty(MessageParameter.ARRIVAL_TIME, predictedTime.toString());
            properties.setProperty(MessageParameter.DESIRED_BUS_LINE, pedestrian.getPreferredBusLine());
            msg.setAllUserDefinedParameters(properties);

            send(msg);
            print("Sent INFORM to Station");
        }
    }

    public Pedestrian getPedestrian() {
        return pedestrian;
    }
}
