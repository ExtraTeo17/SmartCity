package Agents;

import LightStrategies.BasicLightStrategy;
import LightStrategies.LightStrategy;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightAgent extends Agent {
    public LightColor lightColor = LightColor.RED;
    public List<String> queue = new ArrayList<>();
    private GeoPosition position;
    BasicLightStrategy strategy = new BasicLightStrategy();

    public TrafficLightAgent(GeoPosition pos) {
        position = pos;
    }

    public GeoPosition getPosition() {
        return position;
    }

    protected void setup() {
        Print("I'm a traffic light.");
        Print("Red light.");
        strategy.ApplyStrategy(this); // kiedy strategy będzie gotowa, to odkomentować
    }

    public void takeDown() {
        super.takeDown();
    }

    public void Print(String message) {
        System.out.println(getLocalName() + ": " + message);
    }
}
