package LightStrategies;

import java.util.HashSet;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import Agents.LightManager;

public abstract class LightStrategy {
    public abstract void ApplyStrategy(LightManager agent);
	public abstract void drawCrossroad(List<Painter<JXMapViewer>> waypointPainter);
}
