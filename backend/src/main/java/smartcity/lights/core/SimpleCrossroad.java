package smartcity.lights.core;

import com.google.common.eventbus.EventBus;
import events.SwitchLightsStartEvent;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.core.IGeoPosition;
import smartcity.lights.OptimizationResult;
import smartcity.lights.abstractions.ICrossroad;
import smartcity.stations.ArrivalInfo;
import utilities.Siblings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class SimpleCrossroad implements ICrossroad {
    private static final Logger logger = LoggerFactory.getLogger(SimpleCrossroad.class);

    private final EventBus eventBus;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<Long, Light> wayIdToLightMap;

    SimpleCrossroad(EventBus eventBus,
                    Siblings<SimpleLightGroup> lightGroups) {
        this.wayIdToLightMap = new HashMap<>() {{
            putAll(lightGroups.first.prepareMap());
            putAll(lightGroups.second.prepareMap());
        }};
        this.eventBus = eventBus;
    }

    @Override
    public void startLifetime() {
        eventBus.post(new SwitchLightsStartEvent(wayIdToLightMap.values()));
    }

    @Override
    public OptimizationResult requestOptimizations() {
        return allCarsOnGreen();
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
                // TODO: Sth is wrong here - pedestrianName <-> add-CAR-GrantedPassthrough?
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
}