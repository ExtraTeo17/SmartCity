package Agents;

import Routing.LightManagerNode;
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
	public final static String VEHICLE = "Vehicle";
    Behaviour move = new CyclicBehaviour() { // TO DO: generalization
        @Override
        public void action() {
            if (Vehicle.isAtTrafficLights()) //When car arrives at the traffic light
            {
            	System.out.println("kotik");
                if (!sentRequestWhen) {
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
                    msg.addReceiver(new AID("LightManager" + Vehicle.getCurrentTrafficLightID(), AID.ISLOCALNAME));
                    Properties properties = new Properties();
                    properties.setProperty("type", VEHICLE);
                    properties.setProperty("adjacentOsmWayId", Long.toString(Vehicle.getAdjacentOsmWayId()));
                    msg.setAllUserDefinedParameters(properties);
                    send(msg);
                    sentRequestWhen = true;
                    System.out.println("Car: Send message "+msg.getAllUserDefinedParameters());
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
            if (rcv != null)
            {
                switch (rcv.getPerformative()) {
                    case ACLMessage.REQUEST:
                    	 System.out.println("Car: Get message"+rcv.getAllUserDefinedParameters());
                        Vehicle.setAllowedToPass(true);
                        ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                        response.addReceiver(rcv.getSender());
                        Properties properties = new Properties();
                       
                        properties.setProperty("type", VEHICLE);
                        properties.setProperty("adjacentOsmWayId", Long.toString(Vehicle.getAdjacentOsmWayId()));
                        response.setAllUserDefinedParameters(properties);
                        send(response);

                        GetNextStop();
                        sentRequestWhen = false;
                        break;
                    case ACLMessage.AGREE:
                    	 System.out.println("Car:Czekam"+rcv.getAllUserDefinedParameters());
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
    	LightManagerNode nextManager = Vehicle.findNextTrafficLight();
    	//System.out.println(nextManagerId);
        if (nextManager!=null) {
        	
            AID dest = new AID("LightManager" + nextManager.lightManagerId, AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(dest);
            Properties properties = new Properties();
            properties.setProperty("type", VEHICLE);
            properties.setProperty("journeyTime", "10000");
            properties.setProperty("adjacentOsmWayId",Long.toString(nextManager.osmWayId));
            msg.setAllUserDefinedParameters(properties);

            send(msg);
            System.out.println("Car: Zaraz będę"+msg.getAllUserDefinedParameters());
        }
    }

    public void takeDown() {
        super.takeDown();
    }

    void Print(String message) {
        System.out.println(getLocalName() + ": " + message);
    }
}
