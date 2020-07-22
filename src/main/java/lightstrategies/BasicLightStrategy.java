package lightstrategies;

import agents.TrafficLightAgent;
import agents.utils.LightColor;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class BasicLightStrategy {

    public void ApplyStrategy(final TrafficLightAgent agent) {
        Behaviour ReceiveMessage = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = agent.receive();
                if (msg != null) {
                    if (msg.getContent().equals("Pass")) {
                        agent.print(msg.getSender().getLocalName() + " passes light.");
                        agent.queue.remove(msg.getSender().getLocalName());
                    }
                    else {
                        agent.print("Message from " + msg.getSender().getLocalName() + ": " + msg.getContent());
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
                agent.lightColor = (agent.lightColor == LightColor.RED) ? LightColor.GREEN : LightColor.RED;
                if (agent.lightColor == LightColor.RED) {
                    for (String name : agent.queue) {
                        AID dest = new AID(name, AID.ISLOCALNAME);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("Red");
                        msg.addReceiver(dest);
                        agent.send(msg);
                    }
                    agent.print("Red light.");
                }
                else {
                    for (String name : agent.queue) {
                        AID dest = new AID(name, AID.ISLOCALNAME);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("Green");
                        msg.addReceiver(dest);
                        agent.send(msg);
                    }
                    agent.print("Green light.");
                }
            }
        };
        agent.addBehaviour(LightSwitch);
    }
}
