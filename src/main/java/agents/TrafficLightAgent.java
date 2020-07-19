package agents;

import lightstrategies.BasicLightStrategy;
import jade.core.Agent;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightAgent extends Agent {
    public LightColor lightColor = LightColor.RED;
    public List<String> queue = new ArrayList<>();
    BasicLightStrategy strategy = new BasicLightStrategy();
    private GeoPosition position;

    public TrafficLightAgent(GeoPosition pos) {
        position = pos;
    }

    public GeoPosition getPosition() {
        return position;
    }

    protected void setup() {
        Print("I'm a traffic light.");
        Print("Red light.");
        strategy.ApplyStrategy(this);
    }

    public void takeDown() {
        super.takeDown();
    }

    public void Print(String message) {
        System.out.println(getLocalName() + ": " + message);
    }
}
