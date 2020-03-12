import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightAgent extends Agent {
    LightColor lightColor = LightColor.RED;
    List<String> queue = new ArrayList<>();

    protected void setup() {
        System.out.println("I'm a light!");
        System.out.println("Red light.");
        Behaviour ReceiveMessage = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if(msg.getContent().equals("Pass"))
                    {
                        System.out.println(msg.getSender().getLocalName()+" passes light.");
                        queue.remove(msg.getSender().getLocalName());
                    }
                    else
                    {
                        System.out.println("Message from "+ msg.getSender().getLocalName() + ": " + msg.getContent());
                        queue.add(msg.getSender().getLocalName());
                    }

                    block(200);
                }
            }
        };
        addBehaviour(ReceiveMessage);
        Behaviour LightSwitch = new TickerBehaviour(this, 15000) {
            @Override
            public void onTick() {
                lightColor = (lightColor == LightColor.RED)? LightColor.GREEN: LightColor.RED;
                if (lightColor == LightColor.RED) {
                    for(String name: queue){
                        AID dest = new AID(name, AID.ISLOCALNAME);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("Red");
                        msg.addReceiver(dest);
                        send(msg);
                    }
                    System.out.println("Red light.");
                } else {
                    for(String name: queue){
                        AID dest = new AID(name, AID.ISLOCALNAME);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("Green");
                        msg.addReceiver(dest);
                        send(msg);
                        System.out.println("Green light.");
                    }
                }
            }
        };
        addBehaviour(LightSwitch);
    }

    protected void takeDown() {
        super.takeDown();
    }
}
