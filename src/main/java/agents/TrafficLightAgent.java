package agents;

import lightstrategies.BasicLightStrategy;
import jade.core.Agent;
import org.jxmapviewer.viewer.DefaultWaypointRenderer;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightAgent extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(TrafficLightAgent.class);
    public LightColor lightColor = LightColor.RED;
    public List<String> queue = new ArrayList<>();
    private final BasicLightStrategy strategy = new BasicLightStrategy();
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
        logger.info(getLocalName() + ": " + message);
    }
}
