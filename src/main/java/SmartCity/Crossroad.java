package SmartCity;

import java.util.HashSet;

import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

public abstract class Crossroad {
	public abstract void addCarToQueue(String carName, int adjacentOsmWayId);
	public abstract void removeCarFromQueue(String carName, int adjacentOsmWayId);
	public abstract void addPedestrianToQueue(String pedestrianName, int adjacentOsmWayId);
	public abstract void removePedestrianFromQueue(String pedestrianName, int adjacentOsmWayId);
	public abstract OptimizationResult requestOptimizations();
	public abstract boolean isLightGreen(int adjacentOsmWayId);
	public abstract void draw(HashSet lightSet, WaypointPainter<Waypoint> painter);
	public abstract void startLifetime();
}
