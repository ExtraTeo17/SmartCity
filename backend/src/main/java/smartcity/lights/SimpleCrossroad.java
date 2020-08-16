package smartcity.lights;

import agents.utils.LightColor;
import gui.MapWindow;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import osmproxy.MapAccessManager;
import osmproxy.elements.OSMNode;
import routing.IGeoPosition;
import smartcity.MasterAgent;

import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class SimpleCrossroad implements ICrossroad {
    public static final int EXTEND_TIME = 30;
    public static boolean STRATEGY_ACTIVE = true;
    private static final Logger logger = LoggerFactory.getLogger(ICrossroad.class);

    private final Map<Long, Light> lights = new HashMap<>();
    private SimpleLightGroup lightGroup1;
    private SimpleLightGroup lightGroup2;
    private Timer timer;
    private boolean alreadyExtendedGreen = false;

    public SimpleCrossroad(Node crossroad, int managerId) {
        prepareLightGroups(crossroad, managerId);
        prepareTimer();
        prepareLightMap();
    }

    public SimpleCrossroad(OSMNode centerCrossroadNode, int managerId) {
        prepareLightGroups(centerCrossroadNode, managerId);
        prepareTimer();
        prepareLightMap();
    }

    private void prepareLightMap() {
        lights.putAll(lightGroup1.prepareMap());
        lights.putAll(lightGroup2.prepareMap());
    }

    private void prepareLightGroups(Node crossroad, int managerId) {
        lightGroup1 = new SimpleLightGroup(MapAccessManager.getCrossroadGroup(crossroad, 1), LightColor.RED, managerId);
        lightGroup2 = new SimpleLightGroup(MapAccessManager.getCrossroadGroup(crossroad, 3), LightColor.GREEN, managerId);
    }

    private void prepareLightGroups(OSMNode centerCrossroadNode, int managerId) {
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
            logger.info("Illegal state detected", e);
        }
    }

    private void startTimer() {
        int delayBeforeStart = 0;
        int repeatIntervalInMilliseconds = SimpleCrossroad.EXTEND_TIME * 1000 / MapWindow.getTimeScale();
        timer.scheduleAtFixedRate(new SwitchLightsTask(), delayBeforeStart, repeatIntervalInMilliseconds);
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
            }
            else {
                redGroupCars += light.carQueue.size();
                greenGroupCars += light.pedestrianQueue.size();
            }
        }
        if (greenGroupCars > redGroupCars) {
            SimpleCrossroad.logger.info("LM:CROSSROAD HAS PROLONGED GREEN LIGHT FOR " + greenGroupCars + " CARS AS OPPOSED TO " + redGroupCars);
        }
        return greenGroupCars > redGroupCars; // should check if two base green intervals have passed (also temporary, because it also sucks)
    }

    private OptimizationResult allCarsOnGreen() {
        OptimizationResult result = new OptimizationResult();
        for (Light light : lights.values()) {
            if (light.isGreen()) {
                for (String carName : light.carQueue) {
                    result.addCarGrantedPassthrough(carName);
                }
            }
            else {
                for (String pedestrianName : light.pedestrianQueue) {
                    result.addCarGrantedPassthrough(pedestrianName);
                }
            }
        }
        return result;
    }

    @Override
    public void draw(List<Painter<JXMapViewer>> painter) {
        WaypointPainter<Waypoint> painter1 = new WaypointPainter<>();
        WaypointPainter<Waypoint> painter2 = new WaypointPainter<>();
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
            logAddException(carName, adjacentOsmWayId);
        }
    }

    private void logAddException(String name, long adjacentOsmWayId) {
        logger.info("ADD");
        logger.info(String.valueOf(adjacentOsmWayId));
        for (Entry<Long, Light> l : lights.entrySet()) {
            logger.info("-------------");
            logger.info(String.valueOf(l.getKey()));
            logger.info(String.valueOf(l.getValue().getAdjacentOSMWayId()));
        }
        logger.info(name);
    }

    @Override
    public List<IGeoPosition> getLightsPositions() {
        return lights.values().stream().map(light -> (IGeoPosition) light).collect(Collectors.toList());
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
            logAddException(pedestrianName, adjacentOsmWayId);
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

    private class SwitchLightsTask extends TimerTask {

        @Override
        public void run() {
            if (SimpleCrossroad.STRATEGY_ACTIVE) {
                if (!alreadyExtendedGreen) {
                    if (shouldExtendGreenLightBecauseOfCarsOnLight()) {
                        logger.info("-------------------------------------shouldExtendGreenLightBecauseOfCarsOnLight--------------");
                        alreadyExtendedGreen = true;
                        return;
                    }
                    else if (shouldExtendBecauseOfFarAwayQueue()) {
                        prepareTimer();
                        logger.info("-------------------------------------shouldExtendBecauseOfFarAwayQueue--------------");
                        timer.schedule(new SwitchLightsTask(), SimpleCrossroad.EXTEND_TIME * 1000 / MapWindow.getTimeScale());
                        alreadyExtendedGreen = true;
                        return;
                    }
                }
                else {
                    prepareTimer();
                    startTimer();
                    alreadyExtendedGreen = false;
                }
            }
            lightGroup1.switchLights();
            lightGroup2.switchLights();
        }

        private boolean shouldExtendBecauseOfFarAwayQueue() {
            for (Light light : lights.values()) {
                if (light.isGreen()) {
                    Instant current_time = MasterAgent.getSimulationTime().toInstant();
                    for (Instant time_of_car : light.farAwayCarMap.values()) {
                        // If current time + EXTEND_TIME > time_of_car
                        if (current_time.plusSeconds(SimpleCrossroad.EXTEND_TIME).isAfter(time_of_car)) {
                            logger.info("---------------------------------------------WHY WE should extend " +
                                    time_of_car + "----------Current time" + current_time);
                            return true;
                        }
                    }
                }
                else {
                    Instant current_time = MasterAgent.getSimulationTime().toInstant();
                    for (Instant time_of_pedestrian : light.farAwayPedestrianMap.values()) {
                        // If current time + EXTEND_TIME > time_of_car
                        if (current_time.plusSeconds(SimpleCrossroad.EXTEND_TIME).isAfter(time_of_pedestrian)) {
                            logger.info("---------------------------------------------WHY WE should extend " +
                                    time_of_pedestrian + "----------Current time" + current_time);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
}
