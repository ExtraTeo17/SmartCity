package Agents;

import java.util.HashSet;

import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import LightStrategies.LightManagerStrategy;
import LightStrategies.LightStrategy;
import jade.core.Agent;

public class LightManager extends Agent {
	
    private final LightStrategy strategy = new LightManagerStrategy();
    
    protected void setup() {
        print("I'm a traffic manager.");
        strategy.ApplyStrategy(this);
    }

    public void takeDown() {
        super.takeDown();
    }

    public void print(String message) {
        System.out.println(getLocalName() + ": " + message);
    }

	public void draw(HashSet set, WaypointPainter<Waypoint> waypointPainter) {
		strategy.drawCrossroad(set, waypointPainter);
	}
}
