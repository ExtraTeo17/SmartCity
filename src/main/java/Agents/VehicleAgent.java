package Agents;

import Vehicles.DummyCar;
import Vehicles.Vehicle;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class VehicleAgent extends Agent { // TO ADD SOME WRAPPER AROUND POINTLIST IN VEHICLE SO IT CONTAINS ADJACENTWAYIDS AND ITS CONSECUTIVE MANAGERS ALL ALONG !!!
    public Vehicle Vehicle;
    LightColor currentLightColor;


    Behaviour move = new CyclicBehaviour() { // TO DO: generalization
        @Override
        public void action() {
            if(Vehicle.isAtTrafficLights()) //When car arrives at the traffic light
            {
                if(currentLightColor == LightColor.GREEN){
                    Print("Passing green light.");
                    AID dest = new AID("Light" + Vehicle.getCurrentTrafficLightID(), AID.ISLOCALNAME);
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setContent("Pass");
                    msg.addReceiver(dest);
                    send(msg);
                    GetNextStop();
                    Vehicle.Move();
                    Print(Vehicle.getPositionString());
                }
                else
                    Print("Waiting for green.");
            }
            else if(Vehicle.isAtDestination()){
                Print("Reached destination.");
                doDelete();
            }
            else {
                Vehicle.Move();
                Print(Vehicle.getPositionString());
            }
            block(1000);
        }
    };

    public void setVehicle(Vehicle v)
    {
        Vehicle = v;
    }

    protected void setup() {
        if(Vehicle == null)
        {
            String type = (String) getArguments()[0];
            if(type.equals("BasicCar")) // check created vehicle type
            {
                Vehicle = new DummyCar();
            }
            else{ // if type was not found, create Basic Car
                Vehicle = new DummyCar();
            }
        }
        Print("I'm a "+ Vehicle.getVehicleType() + ".");
        Vehicle.CalculateRoute();
        Print("Starting at: " + Vehicle.getPositionString());
        GetNextStop();

        Behaviour receiveMessages = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    Print("Message from "+ msg.getSender().getLocalName() + ": " + msg.getContent());
                    // receiving next color light
                    if(msg.getContent().equals("Green"))
                    {
                        currentLightColor = LightColor.GREEN;
                    }
                    else if(msg.getContent().equals("Red"))
                    {
                        currentLightColor = LightColor.RED;
                    }
                    block(200);
                }
            }
        };
        addBehaviour(receiveMessages);
        addBehaviour(move);
    }

    void GetNextStop(){ // finds next traffic light and announces his arrival
        if(Vehicle.findNextTrafficLight()) {
            AID dest = new AID("Light" + Vehicle.getCurrentTrafficLightID(), AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("On my way.");
            msg.addReceiver(dest);
            send(msg);
        }
    }

    public void takeDown() {
        super.takeDown();
    }

    void Print(String message){
        System.out.println(getLocalName() + ": " + message);
    }
}
