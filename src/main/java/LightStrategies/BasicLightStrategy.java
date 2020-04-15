package LightStrategies;

import Agents.LightColor;
import Agents.TrafficLightAgent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class BasicLightStrategy extends LightStrategy {
    @Override
    public void ApplyStrategy(final TrafficLightAgent agent) {
        Behaviour ReceiveMessage = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = agent.receive();
                if (msg != null) {
                    if(msg.getContent().equals("Pass"))
                    {
                        agent.Print(msg.getSender().getLocalName()+" passes light.");
                        agent.queue.remove(msg.getSender().getLocalName());
                    }
                    else
                    {
                        agent.Print("Message from "+ msg.getSender().getLocalName() + ": " + msg.getContent());
                        agent.queue.add(msg.getSender().getLocalName());
                    }

                    block(200);
                }
            }
        };
        agent.addBehaviour(ReceiveMessage);

        Behaviour LightSwitch = new TickerBehaviour(agent, 15000) {
            @Override
            public void onTick() {
                agent.lightColor = (agent.lightColor == LightColor.RED)? LightColor.GREEN: LightColor.RED;
                if (agent.lightColor == LightColor.RED) {
                    for(String name: agent.queue){
                        AID dest = new AID(name, AID.ISLOCALNAME);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("Red");
                        msg.addReceiver(dest);
                        agent.send(msg);
                    }
                    agent.Print("Red light.");
                } else {
                    for(String name: agent.queue){
                        AID dest = new AID(name, AID.ISLOCALNAME);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("Green");
                        msg.addReceiver(dest);
                        agent.send(msg);
                    }
                    agent.Print("Green light.");
                }
            }
        };
        agent.addBehaviour(LightSwitch);
    }
}
