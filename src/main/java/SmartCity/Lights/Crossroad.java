package SmartCity.Lights;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

public abstract class Crossroad {
	public abstract void addCarToQueue(String carName, long adjacentOsmWayId);
	public abstract void addCarToFarAwayQueue(String carName, long adjacentOsmWayId, Instant journeyTime);
	public abstract void removeCarFromQueue(long adjacentOsmWayId);
	public abstract void removeCarFromFarAwayQueue(String carName, long adjacentOsmWayId);
	public abstract void addPedestrianToQueue(String pedestrianName, long adjacentOsmWayId);
	public abstract void addPedestrianToFarAwayQueue(String pedestrianName, long adjacentOsmWayId, Instant journeyTime);
	public abstract void removePedestrianFromQueue(long adjacentOsmWayId);
	public abstract void removePedestrianFromFarAwayQueue(String pedestrianName, long adjacentOsmWayId);
	public abstract OptimizationResult requestOptimizations();
	public abstract boolean isLightGreen(long adjacentOsmWayId);
	public abstract void draw( List<Painter<JXMapViewer>>painter);
	public abstract void startLifetime();
}
