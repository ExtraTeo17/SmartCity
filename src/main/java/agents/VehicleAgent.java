package agents;

import routing.LightManagerNode;
import smartcity.SmartCityAgent;
import vehicles.DrivingState;
import vehicles.MovingObject;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;

import java.time.Instant;

@SuppressWarnings("serial")
public class VehicleAgent extends Agent {

    private MovingObject Vehicle;

    @Override
    protected void setup() {
        GetNextStop();
        Vehicle.setState(DrivingState.MOVING);

        int speed = Vehicle.getSpeed();
        Behaviour move = new TickerBehaviour(this, 3600 / speed) {
            @Override
            public void onTick() {
                if (Vehicle.isAtTrafficLights()) {
                    switch (Vehicle.getState()) {
                        case MOVING:
                            Vehicle.setState(DrivingState.WAITING_AT_LIGHT);
                            LightManagerNode light = Vehicle.getCurrentTrafficLightNode();
                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
                            msg.addReceiver(new AID("LightManager" + light.getLightManagerId(), AID.ISLOCALNAME));
                            Properties properties = new Properties();
                            properties.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(Vehicle.getAdjacentOsmWayId()));
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                            Print("Asking LightManager" + light.getLightManagerId() + " for right to passage.");
                            break;
                        case WAITING_AT_LIGHT:

                            break;
                        case PASSING_LIGHT:
                            Print("Passing");
                            Vehicle.Move();
                            Vehicle.setState(DrivingState.MOVING);
                            break;
                    }
                }
                else if (Vehicle.isAtDestination()) {
                    Vehicle.setState(DrivingState.AT_DESTINATION);
                    Print("Reached destination.");

                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID("SmartCityAgent", AID.ISLOCALNAME));
                    Properties prop = new Properties();
                    prop.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
                    prop.setProperty(MessageParameter.AT_DESTINATION, String.valueOf(Boolean.TRUE));
                    msg.setAllUserDefinedParameters(prop);
                    send(msg);
                    doDelete();
                }
                else {
                    Vehicle.Move();
                }
            }
        };

        Behaviour communication = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv != null) {
                    switch (rcv.getPerformative()) {
                        case ACLMessage.REQUEST:
                            ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                            response.addReceiver(rcv.getSender());
                            Properties properties = new Properties();

                            properties.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(Vehicle.getAdjacentOsmWayId()));
                            response.setAllUserDefinedParameters(properties);
                            send(response);

                            GetNextStop();
                            Vehicle.setState(DrivingState.PASSING_LIGHT);
                            break;
                        case ACLMessage.AGREE:
                            Vehicle.setState(DrivingState.WAITING_AT_LIGHT);
                            break;
                    }
                }
                block(100);
            }
        };

        addBehaviour(move);
        addBehaviour(communication);
    }

    void GetNextStop() {
        // finds next traffic light and announces his arrival
        LightManagerNode nextManager = Vehicle.findNextTrafficLight();

        if (nextManager != null) {

            AID dest = new AID("LightManager" + nextManager.getLightManagerId(), AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(dest);
            Properties properties = new Properties();
            Instant time = SmartCityAgent.getSimulationTime().toInstant().plusMillis(Vehicle.getMillisecondsToNextLight());
            properties.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + time);
            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, "" + nextManager.getOsmWayId());
            msg.setAllUserDefinedParameters(properties);

            send(msg);
            Print("Sending INFORM to LightManager" + nextManager.getLightManagerId() + ".");
        }
    }

    public MovingObject getVehicle() {
        return Vehicle;
    }

    public void setVehicle(MovingObject v) {
        Vehicle = v;
    }

    @Override
    public void takeDown() {
        super.takeDown();
    }

    void Print(String message) {
        //System.out.println(getLocalName() + ": " + message);
    }
}