package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.LightColor;
import com.google.common.eventbus.EventBus;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import routing.core.IGeoPosition;
import smartcity.ITimeProvider;

import java.util.ArrayList;
import java.util.List;

// TODO: Unused?
public class TrafficLightAgent extends AbstractAgent {
    public static final String name = StationAgent.class.getSimpleName().replace("Agent", "");

    private final IGeoPosition position;
    private LightColor lightColor;
    private final List<String> agentsQueue;

    public TrafficLightAgent(int id, ITimeProvider timeProvider,
                             IGeoPosition position, EventBus eventBus) {
        super(id, name, timeProvider, eventBus);
        this.position = position;
        this.lightColor = LightColor.RED;
        this.agentsQueue = new ArrayList<>();
    }

    @Override
    protected void setup() {
        print("I'm a traffic light.");
        print("Red light.");

        var switchLights = new TickerBehaviour(this, 15000) {
            @Override
            public void onTick() {
                var lightColor = getLightColor();
                setLightColor(lightColor.next());
                if (lightColor == LightColor.RED) {
                    for (String name : getWaitingAgents()) {
                        ACLMessage msg = createMessage(ACLMessage.INFORM, name);
                        msg.setContent("Red");
                        send(msg);
                    }
                    print("Red light.");
                }
                else {
                    for (String name : getWaitingAgents()) {
                        ACLMessage msg = createMessage(ACLMessage.INFORM, name);
                        msg.setContent("Green");
                        send(msg);
                    }
                    print("Green light.");
                }
            }
        };

        var communicate = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    // TODO: I don't see it ('Pass') anywhere else, is it valid case?
                    if (msg.getContent().equals("Pass")) {
                        print(msg.getSender().getLocalName() + " passes light.");
                        removeAgentFromQueue(msg.getSender().getLocalName());
                    }
                    else {
                        print("Message from " + msg.getSender().getLocalName() + ": " + msg.getContent());
                        addAgentToQueue(msg.getSender().getLocalName());
                    }

                    block(200);
                }
            }
        };

        addBehaviour(switchLights);
        addBehaviour(communicate);
    }

    public IGeoPosition getPosition() {
        return position;
    }

    public LightColor getLightColor() {
        return lightColor;
    }

    public void setLightColor(LightColor lightColor) {
        this.lightColor = lightColor;
    }

    // TODO: Is the name of method correct??
    public Iterable<String> getWaitingAgents() {
        return agentsQueue;
    }

    public void addAgentToQueue(String name) {
        agentsQueue.add(name);
    }

    public void removeAgentFromQueue(String name) {
        agentsQueue.remove(name);
    }
}
