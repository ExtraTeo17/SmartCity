package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import events.web.bike.BikeAgentUpdatedEvent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import routing.core.IGeoPosition;
import routing.nodes.LightManagerNode;
import smartcity.ITimeProvider;
import smartcity.SmartCityAgent;
import vehicles.MovingObject;
import vehicles.enums.DrivingState;

import static agents.message.MessageManager.createMessage;
import static agents.message.MessageManager.createProperties;
import static routing.RoutingConstants.STEP_CONSTANT;
import static smartcity.config.StaticConfig.USE_BATCHED_UPDATES;

public class BikeAgent extends AbstractAgent {
    private final MovingObject vehicle;

    BikeAgent(int id, MovingObject vehicle,
              ITimeProvider timeProvider,
              EventBus eventBus) {
        super(id, vehicle.getVehicleType(), timeProvider, eventBus);
        this.vehicle = vehicle;
    }

    @Override
    protected void setup() {
        informLightManager(vehicle);
        vehicle.setState(DrivingState.MOVING);
        int speed = vehicle.getSpeed();
        if (speed > STEP_CONSTANT) {
            print("Invalid speed: " + speed + "\n   Terminating!!!   \n");
            doDelete();
            return;
        }
        Behaviour move = new TickerBehaviour(this, STEP_CONSTANT / speed) {
            @Override
            public void onTick() {
                if (vehicle.isAtTrafficLights()) {
                    switch (vehicle.getState()) {
                        case MOVING:
                            vehicle.setState(DrivingState.WAITING_AT_LIGHT);
                            LightManagerNode light = vehicle.getCurrentTrafficLightNode();
                            ACLMessage msg = createMessageById(ACLMessage.REQUEST_WHEN, LightManagerAgent.name,
                                    light.getLightManagerId());
                            Properties properties = createProperties(MessageParameter.BIKE);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(vehicle.getAdjacentOsmWayId()));
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                            print("Asking LightManager" + light.getLightManagerId() + " for right to passage.");
                            break;
                        case WAITING_AT_LIGHT:

                            break;
                        case PASSING_LIGHT:
                            print("Passing");
                            move();
                            vehicle.setState(DrivingState.MOVING);
                            break;
                    }
                }
                else if (vehicle.isAtDestination()) {
                    vehicle.setState(DrivingState.AT_DESTINATION);
                    print("Reached destination.");

                    ACLMessage msg = createMessage(ACLMessage.INFORM, SmartCityAgent.name);
                    var prop = createProperties(MessageParameter.BIKE);
                    prop.setProperty(MessageParameter.AT_DESTINATION, String.valueOf(Boolean.TRUE));
                    msg.setAllUserDefinedParameters(prop);
                    send(msg);
                    doDelete();
                }
                else {
                    move();
                }
            }
        };

        Behaviour communication = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv == null) {
                    block();
                    return;
                }

                switch (rcv.getPerformative()) {
                    case ACLMessage.REQUEST -> {
                        ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());
                        Properties properties = createProperties(MessageParameter.BIKE);
                        properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID,
                                Long.toString(vehicle.getAdjacentOsmWayId()));
                        response.setAllUserDefinedParameters(properties);
                        send(response);

                        informLightManager(vehicle);
                        vehicle.setState(DrivingState.PASSING_LIGHT);
                    }
                    case ACLMessage.AGREE -> vehicle.setState(DrivingState.WAITING_AT_LIGHT);

                }
            }


        };

        addBehaviour(move);
        addBehaviour(communication);
    }

    public MovingObject getVehicle() {
        return vehicle;
    }

    public void move() {
        vehicle.move();
        if (!USE_BATCHED_UPDATES) {
            eventBus.post(new BikeAgentUpdatedEvent(this.getId(), vehicle.getPosition()));
        }
    }

    public IGeoPosition getPosition() {
        return vehicle.getPosition();
    }
}
