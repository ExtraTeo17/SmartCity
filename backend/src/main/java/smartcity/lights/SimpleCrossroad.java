package smartcity.lights;

import agents.utilities.LightColor;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import osmproxy.elements.OSMNode;
import routing.core.IGeoPosition;
import smartcity.ITimeProvider;
import smartcity.TimeProvider;
import smartcity.stations.ArrivalInfo;
import utilities.Siblings;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SimpleCrossroad implements ICrossroad {
    public static final int EXTEND_TIME_SECONDS = 30;
    public static boolean STRATEGY_ACTIVE = true;
    private static final Logger logger = LoggerFactory.getLogger(SimpleCrossroad.class);

    private final ITimeProvider timeProvider;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<Long, Light> wayIdToLightMap;

    private Timer timer;
    private boolean alreadyExtendedGreen = false;

    private SimpleCrossroad(ITimeProvider timeProvider,
                            Siblings<SimpleLightGroup> lightGroups) {
        this.timeProvider = timeProvider;
        this.wayIdToLightMap = new HashMap<>() {{
            putAll(lightGroups.first.prepareMap());
            putAll(lightGroups.second.prepareMap());
        }};

        prepareTimer();
    }

    public SimpleCrossroad(ITimeProvider timeProvider,
                           Node crossroad,
                           int managerId) {
        this(timeProvider, getLightGroups(crossroad, managerId));
    }

    public SimpleCrossroad(ITimeProvider timeProvider,
                           OSMNode centerCrossroadNode,
                           int managerId) {
        this(timeProvider, getLightGroups(centerCrossroadNode, managerId));
    }

    private static Siblings<SimpleLightGroup> getLightGroups(Node crossroad, int managerId) {
        var crossroadChildren = crossroad.getChildNodes();
        var lightGroupA = new SimpleLightGroup(crossroadChildren.item(1), LightColor.RED, managerId);
        var lightGroupB = new SimpleLightGroup(crossroadChildren.item(3), LightColor.GREEN, managerId);

        return Siblings.of(lightGroupA, lightGroupB);
    }

    private static Siblings<SimpleLightGroup> getLightGroups(OSMNode centerCrossroadNode, int managerId) {
        var info = new CrossroadInfo(centerCrossroadNode);
        var lightGroupA = new SimpleLightGroup(info.getFirstLightGroupInfo(), LightColor.RED, managerId);
        var lightGroupB = new SimpleLightGroup(info.getSecondLightGroupInfo(), LightColor.GREEN, managerId);

        return Siblings.of(lightGroupA, lightGroupB);
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
        int repeatIntervalInMilliseconds = SimpleCrossroad.EXTEND_TIME_SECONDS * 1000 / TimeProvider.TIME_SCALE;
        timer.scheduleAtFixedRate(new SwitchLightsTask(), delayBeforeStart, repeatIntervalInMilliseconds);
    }

    @Override
    public OptimizationResult requestOptimizations() {
        return allCarsOnGreen();
    }

    private boolean shouldExtendGreenLightBecauseOfCarsOnLight() {
        int greenGroupCars = 0;
        int redGroupCars = 0;
        for (Light light : wayIdToLightMap.values()) {
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
            logger.info("LM:CROSSROAD HAS PROLONGED GREEN LIGHT FOR " + greenGroupCars + " CARS AS OPPOSED TO " + redGroupCars);
        }

        // TODO: should check if two base green intervals have passed (also temporary, because it also sucks)
        return greenGroupCars > redGroupCars;
    }

    private OptimizationResult allCarsOnGreen() {
        OptimizationResult result = new OptimizationResult();
        for (Light light : wayIdToLightMap.values()) {
            if (light.isGreen()) {
                for (String carName : light.carQueue) {
                    result.addCarGrantedPassthrough(carName);
                }
            }
            else {
                // TODO: Sth is wrong here
                for (String pedestrianName : light.pedestrianQueue) {
                    result.addCarGrantedPassthrough(pedestrianName);
                }
            }
        }
        return result;
    }

    @Override
    public void draw(List<Painter<JXMapViewer>> painters) {
        WaypointPainter<Waypoint> painter = new WaypointPainter<>();
        var waypointsSet = new HashSet<Waypoint>();
        for (Light light : wayIdToLightMap.values()) {
            light.draw(waypointsSet, painter);
        }
        painter.setWaypoints(waypointsSet);
        painters.add(painter);
    }

    @Override
    public void startLifetime() {
        startTimer();
    }

    @Override
    public List<IGeoPosition> getLightsPositions() {
        return wayIdToLightMap.values().stream().map(light -> (IGeoPosition) light).collect(Collectors.toList());
    }

    private boolean tryConsume(long adjacentWayId, Consumer<Light> consumer) {
        var light = wayIdToLightMap.get(adjacentWayId);
        if (light == null) {
            logAddError(adjacentWayId);
            return false;
        }

        consumer.accept(light);
        return true;
    }

    private void logAddError(long adjacentWayId) {
        logger.warn("Failed to get adjacentWayId: " + adjacentWayId);
        for (var entry : wayIdToLightMap.entrySet()) {
            logger.warn("-------------\n " +
                    entry.getKey() + "\n " +
                    entry.getValue().getAdjacentWayId());
        }
    }

    @Override
    public boolean addCarToQueue(long adjacentWayId, String agentName) {
        return tryConsume(adjacentWayId, l -> l.addCarToQueue(agentName));
    }

    @Override
    public boolean removeCarFromQueue(long adjacentWayId) {
        return tryConsume(adjacentWayId, Light::removeCarFromQueue);
    }

    @Override
    public boolean addCarToFarAwayQueue(long adjacentWayId, ArrivalInfo arrivalInfo) {
        return tryConsume(adjacentWayId, l -> l.addCarToFarAwayQueue(arrivalInfo));
    }

    @Override
    public boolean removeCarFromFarAwayQueue(long adjacentWayId, String agentName) {
        return tryConsume(adjacentWayId, light -> light.removeCarFromFarAwayQueue(agentName));
    }


    @Override
    public boolean addPedestrianToQueue(long adjacentWayId, String agentName) {
        return tryConsume(adjacentWayId, light -> light.addPedestrianToQueue(agentName));
    }

    @Override
    public boolean addPedestrianToFarAwayQueue(long adjacentWayId, ArrivalInfo arrivalInfo) {
        return tryConsume(adjacentWayId, light -> light.addPedestrianToFarAwayQueue(arrivalInfo));
    }

    @Override
    public boolean removePedestrianFromQueue(long adjacentWayId) {
        return tryConsume(adjacentWayId, Light::removePedestrianFromQueue);
    }

    @Override
    public boolean removePedestrianFromFarAwayQueue(long adjacentWayId, String agentName) {
        return tryConsume(adjacentWayId, l -> l.removePedestrianFromFarAwayQueue(agentName));
    }

    // TODO: Move this task to TaskManager
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
                        timer.schedule(new SwitchLightsTask(), EXTEND_TIME_SECONDS * 1000 / TimeProvider.TIME_SCALE);
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

            for (var light : wayIdToLightMap.values()) {
                light.switchLight();
            }
        }

        private boolean shouldExtendBecauseOfFarAwayQueue() {
            for (Light light : wayIdToLightMap.values()) {
                var currentTime = timeProvider.getCurrentSimulationTime();
                var currentTimePlusExtend = currentTime.plusSeconds(EXTEND_TIME_SECONDS);

                var timeCollection = light.isGreen() ?
                        light.farAwayCarMap.values() :
                        light.farAwayPedestrianMap.values();
                for (var time : timeCollection) {
                    if (currentTimePlusExtend.isAfter(time)) {
                        logger.info("Extending, time=" + time + ", currentTime=" + currentTime);
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
