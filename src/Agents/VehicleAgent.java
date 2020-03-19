package Agents;

import Vehicles.BasicCar;
import Vehicles.Vehicle;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class VehicleAgent extends Agent {
    Vehicle vehicle;
    LightColor currentLightColor;

    protected void setup() {
        String type = (String) getArguments()[0];
        if(type.equals("BasicCar")) // check created vehicle type
        {
            vehicle = new BasicCar();
        }
        else{ // if type was not found, create Basic Car
            vehicle = new BasicCar();
        }
        Print("I'm a "+ vehicle.getVehicleType() + ".");
        vehicle.CalculatePath();
        GetNextStop();
        Behaviour move = new CyclicBehaviour() { // TO DO: generalization
            @Override
            public void action() {
                if(vehicle.isAtTrafficLights()) //When car arrives at the traffic light
                {
                    if(currentLightColor == LightColor.GREEN){
                        Print("Passing green light.");
                        AID dest = new AID("Light" + vehicle.getCurrentTrafficLightID(), AID.ISLOCALNAME);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("Pass");
                        msg.addReceiver(dest);
                        send(msg);
                        GetNextStop();
                        vehicle.Move();
                        Print(vehicle.getPositionString());
                    }
                    else
                        Print("Waiting for green.");
                }
                else if(vehicle.isAtDestination()){
                    Print("Reached destination.");
                    doDelete();
                }
                else {
                    vehicle.Move();
                    Print(vehicle.getPositionString());
                }
                block(1000);
            }
        };
        addBehaviour(move);

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
    }

    void GetNextStop(){ // finds next traffic light and announces his arrival
        if(vehicle.findNextTrafficLight()) {
            AID dest = new AID("Light" + vehicle.getCurrentTrafficLightID(), AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("On my way.");
            msg.addReceiver(dest);
            send(msg);
        }
    }

    protected void takeDown() {
        super.takeDown();
    }

    void Print(String message){
        System.out.println(getLocalName() + ": " + message);
    }
}
