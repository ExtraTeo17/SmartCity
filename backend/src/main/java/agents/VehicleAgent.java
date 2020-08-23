package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import routing.LightManagerNode;
import routing.Router;
import routing.core.IGeoPosition;
import smartcity.MasterAgent;
import vehicles.DrivingState;
import vehicles.MovingObject;

@SuppressWarnings("serial")
// TODO: Maybe rename to CarAgent? Bus is also a Vehicle
public class VehicleAgent extends AbstractAgent {
    private final MovingObject vehicle;

    VehicleAgent(int id, MovingObject vehicle) {
        super(id);
        this.vehicle = vehicle;
    }

    @Override
    public String getNamePrefix() {
        return vehicle.getVehicleType();
    }

    @Override
    protected void setup() {
        vehicle.getNextTrafficLight();
        vehicle.setState(DrivingState.MOVING);

        int speed = vehicle.getSpeed();
        Behaviour move = new TickerBehaviour(this, Router.STEP_CONSTANT / speed) {
            @Override
            public void onTick() {
                if (vehicle.isAtTrafficLights()) {
                    switch (vehicle.getState()) {
                        case MOVING:
                            vehicle.setState(DrivingState.WAITING_AT_LIGHT);
                            LightManagerNode light = vehicle.getNextTrafficLight();
                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
                            msg.addReceiver(new AID("LightManager" + light.getLightManagerId(), AID.ISLOCALNAME));
                            Properties properties = new Properties();
                            properties.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(vehicle.getAdjacentOsmWayId()));
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                            print("Asking LightManager" + light.getLightManagerId() + " for right to passage.");
                            break;
                        case WAITING_AT_LIGHT:

                            break;
                        case PASSING_LIGHT:
                            print("Passing");
                            vehicle.move();
                            vehicle.setState(DrivingState.MOVING);
                            break;
                    }
                }
                else if (vehicle.isAtDestination()) {
                    vehicle.setState(DrivingState.AT_DESTINATION);
                    print("Reached destination.");

                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID(MasterAgent.name, AID.ISLOCALNAME));
                    Properties prop = new Properties();
                    prop.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
                    prop.setProperty(MessageParameter.AT_DESTINATION, String.valueOf(Boolean.TRUE));
                    msg.setAllUserDefinedParameters(prop);
                    send(msg);
                    doDelete();
                }
                else {
                    vehicle.move();
                }
            }
        };

        Behaviour communication = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv != null) {
                    switch (rcv.getPerformative()) {
                        case ACLMessage.REQUEST -> {
                            ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                            response.addReceiver(rcv.getSender());
                            Properties properties = new Properties();
                            properties.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(vehicle.getAdjacentOsmWayId()));
                            response.setAllUserDefinedParameters(properties);
                            send(response);
                            vehicle.getNextTrafficLight();
                            vehicle.setState(DrivingState.PASSING_LIGHT);
                        }
                        case ACLMessage.AGREE -> vehicle.setState(DrivingState.WAITING_AT_LIGHT);
                    }
                }
                block(100);
            }
        };

        addBehaviour(move);
        addBehaviour(communication);
    }

    public MovingObject getVehicle() {
        return vehicle;
    }

    public IGeoPosition getPosition() {return vehicle.getPosition();}
}
