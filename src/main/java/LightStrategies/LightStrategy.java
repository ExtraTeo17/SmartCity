package LightStrategies;

import java.util.HashSet;

import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import Agents.LightManager;

public abstract class LightStrategy {
    public abstract void ApplyStrategy(LightManager agent);
	public abstract void drawCrossroad(HashSet set, WaypointPainter<Waypoint> waypointPainter);
}
