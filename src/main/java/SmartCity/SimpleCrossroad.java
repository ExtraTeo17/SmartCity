package SmartCity;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import Agents.LightColor;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.w3c.dom.Node;

public class SimpleCrossroad extends Crossroad {
	
	private Map<Integer, Light> lights = new HashMap<>();
	private SimpleLightGroup lightGroup1;
	private SimpleLightGroup lightGroup2;
	private Timer timer;
	
	public SimpleCrossroad(Node crossroad, Long managerId) {
		prepareLightGroups(crossroad, managerId);
		prepareTimer();
	}
	
	private void prepareLightGroups(Node crossroad, Long managerId) {
		lightGroup1 = new SimpleLightGroup(MapAccessManager.getCrossroadGroup(crossroad, 1), LightColor.RED, managerId);
		lightGroup2 = new SimpleLightGroup(MapAccessManager.getCrossroadGroup(crossroad, 3), LightColor.GREEN, managerId);
	}
	
	private void prepareTimer() {
		timer = new Timer(true);
	}

	private void startTimer() {
		int delayBeforeStart = 0;
		int repeatIntervalInMillisecs = 5000;
		timer.scheduleAtFixedRate(new SwitchLightsTask(), delayBeforeStart, repeatIntervalInMillisecs);
	}
	
	private class SwitchLightsTask extends TimerTask {

		@Override
		public void run() {
			lightGroup1.switchLights();
			lightGroup2.switchLights();
		}
		
	}

	@Override
	public void addCarToQueue(String carName, int adjacentOsmWayId) {
		lights.get(adjacentOsmWayId).addCarToQueue(carName);
	}

	@Override
	public void addPedestrianToQueue(String pedestrianName, int adjacentOsmWayId) {
		// TODO Auto-generated method stub
	}

	@Override
	public OptimizationResult requestOptimizations() {
		// TODO: Katsiaryna <33
		return OptimizationResult.empty();
	}

	@Override
	public boolean isLightGreen(int adjacentOsmWayId) {
		return lights.get(adjacentOsmWayId).isGreen();
	}

	@Override
	public void draw(HashSet lightSet, WaypointPainter<Waypoint> painter) {
		lightGroup1.drawLights(lightSet, painter);
		lightGroup2.drawLights(lightSet, painter);
	}

	@Override
	public void removeCarFromQueue(String carName, int adjacentOsmWayId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removePedestrianFromQueue(String pedestrianName, int adjacentOsmWayId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startLifetime() {
		startTimer();
	}
}
