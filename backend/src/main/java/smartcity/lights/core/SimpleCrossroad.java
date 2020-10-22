package smartcity.lights.core;

import com.google.common.eventbus.EventBus;
import events.SwitchLightsStartEvent;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.lights.OptimizationResult;
import smartcity.lights.abstractions.ICrossroad;
import smartcity.stations.ArrivalInfo;
import utilities.Siblings;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class SimpleCrossroad implements ICrossroad {
    private static final int TRAFFIC_JAM_THRESHOLD = 3;
	private final Logger logger;
    private final EventBus eventBus;
    private final int managerId;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<Long, Light> wayIdToLightMap;

    SimpleCrossroad(EventBus eventBus,
                    int managerId,
                    Siblings<SimpleLightGroup> lightGroups) {
        this.logger = LoggerFactory.getLogger("SimpleCrossroad" + managerId);
        this.eventBus = eventBus;
        this.managerId = managerId;
        this.wayIdToLightMap = new HashMap<>() {{
            putAll(lightGroups.first.prepareMap());
            putAll(lightGroups.second.prepareMap());
        }};
    }

    @Override
    public void startLifetime() {
        eventBus.post(new SwitchLightsStartEvent(managerId, wayIdToLightMap.values()));
    }

    @Override
    public OptimizationResult requestOptimizations() {
        return allCarsOnGreen();
    }

    private OptimizationResult allCarsOnGreen() {
        var result = new OptimizationResult();
        for (Light light : wayIdToLightMap.values()) {
            if (light.isGreen()) {
                for (String carName : light.carQueue) {
                    result.addCarGrantedPassthrough(carName);
                    if (light.carQueue.size() > TRAFFIC_JAM_THRESHOLD) {
                    	result.setShouldNotifyCarAboutTrafficJamOnThisLight(light.getLat(), light.getLng());
                    }
                    break;
                }
            }
            else {
                for (String pedestrianName : light.pedestrianQueue) {
                    result.addCarGrantedPassthrough(pedestrianName);
                    break;
                }
            }
        }
        return result;
    }



    @Override
    public void draw(List<Painter<JXMapViewer>> painters) {
        var lights = wayIdToLightMap.values();
        painters.add(getPainterByColor(lights, true));
        painters.add(getPainterByColor(lights, false));
    }

    private Painter<JXMapViewer> getPainterByColor(Collection<Light> lights, boolean isGreen) {
        WaypointPainter<Waypoint> painter = new WaypointPainter<>();
        var waypointsSet = new HashSet<Waypoint>();
        lights.forEach(l -> {
            if (l.isGreen() == isGreen) {
                l.draw(waypointsSet, painter);
            }
        });
        painter.setWaypoints(waypointsSet);
        return painter;
    }

    @Override
    public List<Light> getLights() {
        return new ArrayList<>(wayIdToLightMap.values());
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
        logger.warn("Failed to get adjacentWayId: " + adjacentWayId + "\n" + wayIdToLightMap.entrySet().stream()
                .map(entry -> entry.getKey() + ", " + entry.getValue().getOsmLightId())
                .collect(Collectors.joining("\n")));
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
