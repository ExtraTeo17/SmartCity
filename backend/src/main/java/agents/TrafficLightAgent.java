package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.LightColor;
import behaviourfactories.BasicLightsBehaviourFactory;
import behaviourfactories.IBehaviourFactory;
import routing.core.IGeoPosition;
import smartcity.ITimeProvider;

import java.util.ArrayList;
import java.util.List;

// TODO: Unused?
public class TrafficLightAgent extends AbstractAgent {
    public static final String name = StationAgent.class.getSimpleName().replace("Agent", "");

    // TODO: Inject as dependency
    private final IBehaviourFactory<TrafficLightAgent> behaviourFactory;
    private final IGeoPosition position;
    private LightColor lightColor;
    private final List<String> agentsQueue;

    public TrafficLightAgent(int id, ITimeProvider timeProvider, IGeoPosition position) {
        super(id, name, timeProvider);
        this.position = position;
        this.behaviourFactory = new BasicLightsBehaviourFactory();
        this.lightColor = LightColor.RED;
        this.agentsQueue = new ArrayList<>();
    }

    @Override
    protected void setup() {
        print("I'm a traffic light.");
        print("Red light.");
        addBehaviour(behaviourFactory.createCyclicBehaviour(this));
        addBehaviour(behaviourFactory.createTickerBehaviour(this));
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
