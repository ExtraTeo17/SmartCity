package agents;

import agents.utils.LightColor;
import lightstrategies.BasicLightStrategy;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightAgent extends AbstractAgent {
    private static final Logger logger = LoggerFactory.getLogger(TrafficLightAgent.class);
    public LightColor lightColor = LightColor.RED;
    public List<String> queue = new ArrayList<>();
    private final BasicLightStrategy strategy = new BasicLightStrategy();
    private final GeoPosition position;

    @Override
    public String getNamePrefix() {
        return "TrafficLight";
    }

    public TrafficLightAgent(int id, GeoPosition pos) {
        super(id);
        position = pos;
    }

    public GeoPosition getPosition() {
        return position;
    }

    @Override
    protected void setup() {
        print("I'm a traffic light.");
        print("Red light.");
        strategy.ApplyStrategy(this);
    }

    @Override
    public void takeDown() {
        super.takeDown();
    }
}
