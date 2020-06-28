package SmartCity.Lights;

import java.time.Instant;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import Agents.LightColor;
import GUI.MapWindow;
import LightStrategies.LightManagerStrategy;
import OSMProxy.MapAccessManager;
import OSMProxy.Elements.OSMNode;

import SmartCity.SmartCityAgent;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.w3c.dom.Node;

public class SimpleCrossroad extends Crossroad {

    final public static int EXTEND_TIME = 30;

    private Map<Long, Light> lights = new HashMap<>();
    private SimpleLightGroup lightGroup1;
    private SimpleLightGroup lightGroup2;
    private Timer timer;
    private boolean alreadyExtendedGreen = false;
    public boolean useStrategy = true;

    public SimpleCrossroad(Node crossroad, Long managerId) {
        prepareLightGroups(crossroad, managerId);
        prepareTimer();
        prepareLightMap();
    }

    public SimpleCrossroad(OSMNode centerCrossroadNode, long managerId) {
        prepareLightGroups(centerCrossroadNode, managerId);
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

    private void prepareLightGroups(OSMNode centerCrossroadNode, long managerId) {
        CrossroadInfo info = new CrossroadInfo(centerCrossroadNode);
        lightGroup1 = new SimpleLightGroup(info.getFirstLightGroupInfo(), LightColor.RED, managerId);
        lightGroup2 = new SimpleLightGroup(info.getSecondLightGroupInfo(), LightColor.GREEN, managerId);
    }

    private void prepareTimer() {
        try {
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer(true);
        } catch (IllegalStateException e) {
            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
            return;
        }
    }

    private void startTimer() {
        int delayBeforeStart = 0;
        int repeatIntervalInMillisecs = EXTEND_TIME * 1000 / MapWindow.getTimeScale();
        timer.scheduleAtFixedRate(new SwitchLightsTask(), delayBeforeStart, repeatIntervalInMillisecs);
    }

    private class SwitchLightsTask extends TimerTask {

        @Override
        public void run() {
        	if (STRATEGY_ACTIVE) {
        		if (!alreadyExtendedGreen) {
                    if (shouldExtendGreenLightBecauseOfCarsOnLight()) {
                        System.out.println("-------------------------------------shouldExtendGreenLightBecauseOfCarsOnLight--------------");
                        alreadyExtendedGreen = true;
                        return;
                    } else if (shouldExtendBecauseOfFarAwayQueque()) {
                        prepareTimer();
                        System.out.println("-------------------------------------shouldExtendBecauseOfFarAwayQueque--------------");
                        timer.schedule(new SwitchLightsTask(), EXTEND_TIME * 1000 / MapWindow.getTimeScale());
                        alreadyExtendedGreen = true;
                        return;
                    }
                } else {
                    prepareTimer();
                    startTimer();
                    alreadyExtendedGreen = false;
                }
        	}
            lightGroup1.switchLights();
            lightGroup2.switchLights();
        }

        private boolean shouldExtendBecauseOfFarAwayQueque() {
            for (Light light : lights.values()) {
                if (light.isGreen()) {
                    Instant current_time = SmartCityAgent.getSimulationTime().toInstant();
                    for (Instant time_of_car : light.farAwayCarMap.values()) {
                        // If current time + EXTEND_TIME > time_of_car
                        if (current_time.plusSeconds(EXTEND_TIME).isAfter(time_of_car)) {
                            System.out.println("---------------------------------------------WHY WE should extend " + time_of_car + "----------Curent time" + current_time);
                            return true;
                        }
                    }
                } else {
                    Instant current_time = SmartCityAgent.getSimulationTime().toInstant();
                    for (Instant time_of_pedestrian : light.farAwayPedestrianMap.values()) {
                        // If current time + EXTEND_TIME > time_of_car
                        if (current_time.plusSeconds(EXTEND_TIME).isAfter(time_of_pedestrian)) {
                            System.out.println("---------------------------------------------WHY WE should extend " + time_of_pedestrian + "----------Curent time" + current_time);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    @Override
    public OptimizationResult requestOptimizations() {
        return allCarsOnGreen();
    }

    private boolean shouldExtendGreenLightBecauseOfCarsOnLight() {
        int greenGroupCars = 0;
        int redGroupCars = 0;
        for (Light light : lights.values()) {
            if (light.isGreen()) {
                greenGroupCars += light.carQueue.size(); // temporarily only close queue
                redGroupCars += light.pedestrianQueue.size();
            } else {
                redGroupCars += light.carQueue.size();
                greenGroupCars += light.pedestrianQueue.size();
            }
        }
        if (greenGroupCars > redGroupCars)
            System.out.println("LM:CROSSROAD HAS PROLONGED GREEN LIGHT FOR " + greenGroupCars + " CARS AS OPPOSED TO " + redGroupCars);
        return greenGroupCars > redGroupCars; // should check if two base green intervals have passed (also temporary, because it also sucks)
    }

    private SimpleLightGroup currentGreenGroup() {
        if (lightGroup1.areLightsGreen()) {
            return lightGroup1;
        } else {
            return lightGroup2;
        }
    }

    private OptimizationResult allCarsOnGreen() {
        OptimizationResult result = new OptimizationResult();
        for (Light light : lights.values())
            if (light.isGreen())
                for (String carName : light.carQueue)
                    result.addCarGrantedPassthrough(carName);
            else {
				for(String pedestrianName : light.pedestrianQueue)
					result.addCarGrantedPassthrough(pedestrianName);
            }
        return result;
    }

    @Override
    public boolean isLightGreen(long adjacentOsmWayId) {
        return lights.get(adjacentOsmWayId).isGreen();
    }

    @Override
    public void draw(List<Painter<JXMapViewer>> painter) {
        WaypointPainter<Waypoint> painter1 = new WaypointPainter<Waypoint>();
        WaypointPainter<Waypoint> painter2 = new WaypointPainter<Waypoint>();
        lightGroup1.drawLights(painter1);
        lightGroup2.drawLights(painter2);
        painter.add(painter1);
        painter.add(painter2);
    }

    @Override
    public void startLifetime() {
        startTimer();
    }

    @Override
    public void addCarToFarAwayQueue(String carName, long adjacentOsmWayId, Instant journeyTime) {
        try {
            lights.get(adjacentOsmWayId).addCarToFarAwayQueue(carName, journeyTime);
        } catch (Exception e) {
            System.out.println("ADD");
            System.out.println(adjacentOsmWayId);
            for (Entry<Long, Light> l : lights.entrySet()) {
                System.out.println("-------------");
                System.out.println(l.getKey());
                System.out.println(l.getValue().getAdjacentOSMWayId());
            }
            System.out.println(carName);
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


    @Override
    public void addPedestrianToQueue(String pedestrianName, long adjacentOsmWayId) {
        lights.get(adjacentOsmWayId).addPedestrianToQueue(pedestrianName);
    }

    @Override
    public void addPedestrianToFarAwayQueue(String pedestrianName, long adjacentOsmWayId, Instant journeyTime) {
        try {
            lights.get(adjacentOsmWayId).addPedestrianToFarAwayQueue(pedestrianName, journeyTime);
        } catch (Exception e) {
            System.out.println("ADD");
            System.out.println(adjacentOsmWayId);
            for (Entry<Long, Light> l : lights.entrySet()) {
                System.out.println("-------------");
                System.out.println(l.getKey());
                System.out.println(l.getValue().getAdjacentOSMWayId());
            }
            System.out.println(pedestrianName);
        }
    }

    @Override
    public void removePedestrianFromQueue(long adjacentOsmWayId) {
        lights.get(adjacentOsmWayId).removePedestrianFromQueue();
    }

    @Override
    public void removePedestrianFromFarAwayQueue(String pedestrianName, long adjacentOsmWayId) {
        lights.get(adjacentOsmWayId).removePedestrianFromFarAwayQueue(pedestrianName);
    }
}
