package behaviourfactories;

import agents.TrafficLightAgent;
import agents.utilities.LightColor;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

// TODO: Move to agent
public class BasicLightsBehaviourFactory implements IBehaviourFactory<TrafficLightAgent> {
    @Override
    public CyclicBehaviour createCyclicBehaviour(final TrafficLightAgent agent) {
        return new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = agent.receive();
                if (msg != null) {
                    // TODO: I don't see it ('Pass') anywhere else, is it valid case?
                    if (msg.getContent().equals("Pass")) {
                        agent.print(msg.getSender().getLocalName() + " passes light.");
                        agent.removeAgentFromQueue(msg.getSender().getLocalName());
                    }
                    else {
                        agent.print("Message from " + msg.getSender().getLocalName() + ": " + msg.getContent());
                        agent.addAgentToQueue(msg.getSender().getLocalName());
                    }

                    block(200);
                }
            }
        };
    }

    @Override
    public TickerBehaviour createTickerBehaviour(final TrafficLightAgent agent) {
        return new TickerBehaviour(agent, 15000) {
            @Override
            public void onTick() {
                var lightColor = agent.getLightColor();
                agent.setLightColor(lightColor.next());
                if (lightColor == LightColor.RED) {
                    for (String name : agent.getWaitingAgents()) {
                        AID dest = new AID(name, AID.ISLOCALNAME);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.setContent("Red");
                        msg.addReceiver(dest);
                        agent.send(msg);
                    }
                    agent.print("Red light.");
                }
                else {
                    for (String name : agent.getWaitingAgents()) {
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
    }
}
