package Agents;

import Vehicles.DummyCar;
import Vehicles.Vehicle;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;

public class VehicleAgent extends Agent { // TO ADD SOME WRAPPER AROUND POINTLIST IN VEHICLE SO IT CONTAINS ADJACENTWAYIDS AND ITS CONSECUTIVE MANAGERS ALL ALONG !!!
    public Vehicle Vehicle;

    boolean sentRequestWhen = false;

    Behaviour move = new CyclicBehaviour() { // TO DO: generalization
        @Override
        public void action() {
            if (Vehicle.isAtTrafficLights()) //When car arrives at the traffic light
            {
                if (!sentRequestWhen) {
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
                    msg.addReceiver(new AID("LightManager" + Vehicle.getCurrentTrafficLightID(), AID.ISLOCALNAME));
                    Properties properties = new Properties();
                    properties.setProperty("adjacentOsmWayId", "123");
                    msg.setAllUserDefinedParameters(properties);
                    send(msg);
                    sentRequestWhen = true;
                }
            } else if (Vehicle.isAtDestination()) {
                Print("Reached destination.");
                doDelete();
            } else {
                Vehicle.Move();
                Print(Vehicle.getPositionString());
            }
            block(1000);
        }
    };

    Behaviour communication = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage rcv = receive();
            if(rcv != null)
            {
                switch (rcv.getPerformative()) {
                    case ACLMessage.REQUEST:
                        Vehicle.setAllowedToPass(true);
                        ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                        response.addReceiver(rcv.getSender());
                        Properties properties = new Properties();
                        properties.setProperty("adjacentOsmWayId", "123");
                        response.setAllUserDefinedParameters(properties);
                        send(response);

                        GetNextStop();
                        sentRequestWhen = false;
                        break;
                    case ACLMessage.AGREE:
                        Vehicle.setAllowedToPass(false);
                        break;
                }
            }
            block(1000);
        }
    };

    public void setVehicle(Vehicle v) {
        Vehicle = v;
    }

    protected void setup() {
        if (Vehicle == null) {
            String type = (String) getArguments()[0];
            if (type.equals("BasicCar")) // check created vehicle type
            {
                Vehicle = new DummyCar();
            } else { // if type was not found, create Basic Car
                Vehicle = new DummyCar();
            }
        }
        Print("I'm a " + Vehicle.getVehicleType() + ".");
        Vehicle.CalculateRoute();
        Print("Starting at: " + Vehicle.getPositionString());
        GetNextStop();

        addBehaviour(move);
        addBehaviour(communication);
    }

    void GetNextStop() { // finds next traffic light and announces his arrival
        if (Vehicle.findNextTrafficLight()) {
            AID dest = new AID("LightManager" + Vehicle.getCurrentTrafficLightID(), AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(dest);
            Properties properties = new Properties();
            properties.setProperty("journeyTime", "10000");
            properties.setProperty("adjacentOsmWayId", "123");
            msg.setAllUserDefinedParameters(properties);

            send(msg);
        }
    }

    public void takeDown() {
        super.takeDown();
    }

    void Print(String message) {
        System.out.println(getLocalName() + ": " + message);
    }
}
