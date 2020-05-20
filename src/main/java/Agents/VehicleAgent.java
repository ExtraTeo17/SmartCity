package Agents;

import java.time.Instant;

import Routing.LightManagerNode;
import Vehicles.DrivingState;
import Vehicles.Vehicle;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;

public class VehicleAgent extends Agent { // TO ADD SOME WRAPPER AROUND POINTLIST IN VEHICLE SO IT CONTAINS ADJACENTWAYIDS AND ITS CONSECUTIVE MANAGERS ALL ALONG !!!
    public Vehicle Vehicle;

    public void setVehicle(Vehicle v) {
        Vehicle = v;
    }

    protected void setup() {
        //Print("I'm a " + Vehicle.getVehicleType() + ".");
        //Print("Starting at: " + Vehicle.getPositionString());
        GetNextStop();
        Vehicle.setState(DrivingState.MOVING);

        Behaviour move = new TickerBehaviour(this, 3600 / Vehicle.getSpeed()) { // TO DO: generalization
            @Override
            public void onTick() {
                if (Vehicle.isAtTrafficLights())
                {
                    switch (Vehicle.getState())
                    {
                        case MOVING:
                            LightManagerNode light = Vehicle.getCurrentTrafficLightNode();
                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
                            msg.addReceiver(new AID("LightManager" + light.getLightManagerId(), AID.ISLOCALNAME));
                            Properties properties = new Properties();
                            properties.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(Vehicle.getAdjacentOsmWayId()));
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                            Vehicle.setState(DrivingState.WAITING);
                            Print("Asking LightManager" + light.getLightManagerId() + " for right to passage.");
                            break;
                        case WAITING:
                        	
                            break;
                        case PASSING:
                        	Print("Passing");
                            Vehicle.Move();
                            Vehicle.setState(DrivingState.MOVING);
                            break;
                    }
                } else if (Vehicle.isAtDestination()) {
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
                } else {
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
                        	System.out.println("Car:I can move further, I got request ");
                            ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                            response.addReceiver(rcv.getSender());
                            Properties properties = new Properties();

                            properties.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(Vehicle.getAdjacentOsmWayId()));
                            response.setAllUserDefinedParameters(properties);
                            send(response);

                            GetNextStop();
                            Vehicle.setState(DrivingState.PASSING);
                            break;
                        case ACLMessage.AGREE:
                        	System.out.println("Car:Okej, poczekamy");
                            Vehicle.setState(DrivingState.WAITING);
                            break;
                    }
                }
                block(100);
            }
        };

        addBehaviour(move);
        addBehaviour(communication);
    }

    void GetNextStop() { // finds next traffic light and announces his arrival
        LightManagerNode nextManager = Vehicle.findNextTrafficLight();
        if (nextManager != null) {

            AID dest = new AID("LightManager" + nextManager.getLightManagerId(), AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(dest);
            Properties properties = new Properties();
            Instant time= Instant.now().plusMillis( Vehicle.getMilisecondsToNextLight());
            properties.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" +time);
            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, "" + nextManager.getOsmWayId());
            msg.setAllUserDefinedParameters(properties);

            send(msg);
            Print("Sending INFORM to LightManager" + nextManager.getLightManagerId() + ".");
        }
    }

    public void takeDown() {
        super.takeDown();
    }

    void Print(String message) {
        System.out.println(getLocalName() + ": " + message);
    }
}
