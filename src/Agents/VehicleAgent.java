package Agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class VehicleAgent extends Agent {
    BasicCar car; // TO DO: generalization
    LightColor currentLightColor;
    String nextLightName = "Light8";
    public VehicleAgent() {
        car = new BasicCar();
    }

    protected void setup() {
        System.out.println("I'm a "+ car.getName() +" with a name: " + getLocalName());
        Behaviour move = new CyclicBehaviour() { // TO DO: generalization
            @Override
            public void action() {
                if (car.Position == 0) {
                    AID dest = new AID(nextLightName, AID.ISLOCALNAME);
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setContent("On my way.");
                    msg.addReceiver(dest);
                    send(msg);
                }
                if(car.Position == 8) //When car arrives at the traffic light
                {
                    if(currentLightColor == LightColor.GREEN){
                        System.out.println("Passing green light.");
                        AID dest = new AID(nextLightName, AID.ISLOCALNAME);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("Pass");
                        msg.addReceiver(dest);
                        send(msg);
                        car.Move();
                    }
                    else
                        System.out.println("Waiting for green.");
                }
                else car.Move();
                System.out.println("Position: " + car.Position);
                block(1000);
            }
        };
        addBehaviour(move);

        Behaviour receiveMessages = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println("Message from "+ msg.getSender().getLocalName() + ": " + msg.getContent());
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

    protected void takeDown() {
        super.takeDown();
    }
}
