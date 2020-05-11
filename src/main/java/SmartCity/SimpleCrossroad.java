package SmartCity;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import Agents.LightColor;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.w3c.dom.Node;

public class SimpleCrossroad extends Crossroad {
	
	private Map<Long, Light> lights = new HashMap<>();
	private SimpleLightGroup lightGroup1;
	private SimpleLightGroup lightGroup2;
	private Timer timer;
	
	public SimpleCrossroad(Node crossroad, Long managerId) {
		prepareLightGroups(crossroad, managerId);
		prepareTimer();
		prepareLightMap();
	}
	
	private void prepareLightMap() {
        lights.putAll(lightGroup1.prepareMap());
        lights.putAll(lightGroup2.prepareMap());
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
	public void addPedestrianToQueue(String pedestrianName, long adjacentOsmWayId) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removePedestrianFromQueue(String pedestrianName, long adjacentOsmWayId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public OptimizationResult requestOptimizations() {
		OptimizationResult result = new OptimizationResult();
		/*int allCarsWaitingInGroup1 = lightGroup1.getSumOfCarsWaiting();
		int allCarsWaitingInGroup2 = lightGroup2.getSumOfCarsWaiting();
		if (allCarsWaitingInGroup2 > allCarsWaitingInGroup1)
			switchLightsIfRed(group2);
		else
			switchLightsIfRed(group1);*/
		for (Light light : lights.values())
			if (light.isGreen())
				for (String carName : light.carQueue)
					result.addCarGrantedPassthrough(carName);
		return result;
	}
	
	/*private void switchLightsIfRed(SimpleLightGroup group) {
		group.
	}*/

	@Override
	public boolean isLightGreen(long adjacentOsmWayId) {
		return lights.get(adjacentOsmWayId).isGreen();
	}

	@Override
	public void draw(List<Painter<JXMapViewer>> painter) {
		 WaypointPainter<Waypoint> painter1= new  WaypointPainter<Waypoint> ();
		 WaypointPainter<Waypoint> painter2= new  WaypointPainter<Waypoint> ();
			
		lightGroup1.drawLights( painter1);
		lightGroup2.drawLights( painter2);
		painter.add(painter1);
		painter.add(painter2);
		
	}

	@Override
	public void startLifetime() {
		startTimer();
	}

	@Override
	public void addCarToFarAwayQueue(String carName, long adjacentOsmWayId, int journeyTime) {
	
	try {
		
		lights.get(adjacentOsmWayId).addCarToFarAwayQueue(carName, journeyTime);
		}
	catch (Exception e){
		System.out.println("ADD");
		System.out.println(adjacentOsmWayId);
		for(Entry<Long, Light> l: lights.entrySet()) {
			System.out.println("-------------");
			System.out.println(l.getKey());
			System.out.println(l.getValue().getAdjacentOSMWayId());
			
		}
	System.out.println(carName);
		
		}
	finally {
		
	}
	}

	@Override
	public void addCarToQueue(String carName, long adjacentOsmWayId) {
		lights.get(adjacentOsmWayId).addCarToQueue(carName);
	}

	@Override
	public void removeCarFromFarAwayQueue(String carName, long adjacentOsmWayId) {
		
		lights.get(adjacentOsmWayId).removeCarFromFarAwayQueue(carName);
	}

	@Override
	public void removeCarFromQueue(long adjacentOsmWayId) {
		lights.get(adjacentOsmWayId).removeCarFromQueue();
	}
}
