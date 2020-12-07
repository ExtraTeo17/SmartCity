package smartcity.lights.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import events.LightSwitcherStartedEvent;
import events.SwitchLightsStartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.config.abstractions.ITroublePointsConfigContainer;
import smartcity.lights.OptimizationResult;
import smartcity.lights.abstractions.ICrossroad;
import smartcity.stations.ArrivalInfo;
import utilities.Siblings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class SimpleCrossroad implements ICrossroad {
    private final Logger logger;
    private final EventBus eventBus;
    private final ITroublePointsConfigContainer configContainer;
    private final int managerId;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<Long, Light> wayIdToLightMap;
    private final Map<Long, Light> crossroadIdToLightMap;
    private final Siblings<SimpleLightGroup> lightGroups;
    private final List<Light> allLights;

    private int defaultExecutionDelay = -1;

    SimpleCrossroad(EventBus eventBus,
                    ITroublePointsConfigContainer configContainer,
                    int managerId,
                    Siblings<SimpleLightGroup> lightGroups) {
        this.logger = LoggerFactory.getLogger("SimpleCrossroad" + managerId);
        this.eventBus = eventBus;
        this.configContainer = configContainer;
        this.managerId = managerId;

        this.lightGroups = lightGroups;
        this.allLights = new ArrayList<>();
        this.crossroadIdToLightMap = new HashMap<>();
        this.wayIdToLightMap = new HashMap<>();
        fillLightCollections(lightGroups);
    }

    private void fillLightCollections(Siblings<SimpleLightGroup> lightGroups) {
        for (var light : lightGroups.first) {
            fillLight(light);
        }
        for (var light : lightGroups.second) {
            fillLight(light);
        }
    }

    private void fillLight(Light light) {
        allLights.add(light);

        var crossroadId = Long.parseLong(light.getAdjacentCrossingOsmId1());
        crossroadIdToLightMap.put(crossroadId, light);
        wayIdToLightMap.put(light.getAdjacentWayId(), light);
    }

    @Override
    public void startLifetime() {
        eventBus.register(this);
        eventBus.post(new SwitchLightsStartEvent(managerId, lightGroups));
    }

    @Subscribe
    public void handle(LightSwitcherStartedEvent e) {
        if (e.managerId == this.managerId) {
            this.defaultExecutionDelay = e.defaultExecutionDelay;
            eventBus.unregister(this);
        }
    }

    @Override
    public OptimizationResult requestOptimizations(int extendTimeSeconds) {
        if (!lightSwitcherStarted()) {
            logger.warn("Light switcher did not start yet.");
            return OptimizationResult.empty();
        }

        final OptimizationResult result = new OptimizationResult(extendTimeSeconds, defaultExecutionDelay);
        boolean shouldCheckForJams = configContainer.shouldDetectTrafficJams();

        for (Light light : allLights) {

            if (shouldCheckForJams) {
                light.checkForTrafficJams(result);
            }

            if (light.isGreen()) {
                for (String carName : light.carQueue) {
                    result.addCarGrantedPassthrough(carName);
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

    private boolean lightSwitcherStarted() {
		return defaultExecutionDelay >= 0;
	}

	@Override
    public List<Light> getLights() {
        return new ArrayList<>(allLights);
    }

    @Override
    public boolean addCarToQueue(long adjacentWayId, String agentName) {
        return tryConsumeByWay(adjacentWayId, l -> l.addCarToQueue(agentName));
    }

    @Override
    public boolean removeCarFromQueue(long adjacentWayId) {
        return tryConsumeByWay(adjacentWayId, Light::removeCarFromQueue);
    }

    @Override
    public boolean addCarToFarAwayQueue(long adjacentWayId, ArrivalInfo arrivalInfo) {
        return tryConsumeByWay(adjacentWayId, l -> l.addCarToFarAwayQueue(arrivalInfo));
    }

    @Override
    public boolean removeCarFromFarAwayQueue(long adjacentWayId, String agentName) {
        return tryConsumeByWay(adjacentWayId, light -> light.removeCarFromFarAwayQueue(agentName));
    }

    private boolean tryConsumeByWay(long adjacentWayId, Consumer<Light> consumer) {
        var light = wayIdToLightMap.get(adjacentWayId);
        if (light == null) {
            logAddErrorByWay(adjacentWayId);
            return false;
        }

        consumer.accept(light);
        return true;
    }

    private void logAddErrorByWay(long adjacentWayId) {
        logger.warn("Failed to get adjacentWayId: " + adjacentWayId + "\n" + wayIdToLightMap.entrySet().stream()
                .map(entry -> entry.getKey() + ", " + entry.getValue().getOsmLightId())
                .collect(Collectors.joining("\n")));
    }

    @Override
    public boolean addPedestrianToQueue(long adjacentCrossroadId, String agentName) {
        return tryConsumeByCrossroad(adjacentCrossroadId, light -> light.addPedestrianToQueue(agentName));
    }

    @Override
    public boolean addPedestrianToFarAwayQueue(long adjacentCrossroadId, ArrivalInfo arrivalInfo) {
        return tryConsumeByCrossroad(adjacentCrossroadId, light -> light.addPedestrianToFarAwayQueue(arrivalInfo));
    }

    @Override
    public boolean removePedestrianFromQueue(long adjacentCrossroadId) {
        return tryConsumeByCrossroad(adjacentCrossroadId, Light::removePedestrianFromQueue);
    }

    @Override
    public boolean removePedestrianFromFarAwayQueue(long adjacentCrossroadId, String agentName) {
        return tryConsumeByCrossroad(adjacentCrossroadId, l -> l.removePedestrianFromFarAwayQueue(agentName));
    }

    private boolean tryConsumeByCrossroad(long adjacentCrossroadId, Consumer<Light> consumer) {
        var light = crossroadIdToLightMap.get(adjacentCrossroadId);
        if (light == null) {
            logAddErrorByCrossroad(adjacentCrossroadId);
            return false;
        }

        consumer.accept(light);
        return true;
    }

    private void logAddErrorByCrossroad(long adjacentCrossroadId) {
        logger.warn("Failed to get adjacentCrossroadId: " + adjacentCrossroadId + "\n" + crossroadIdToLightMap.entrySet().stream()
                .map(entry -> entry.getKey() + ", " + entry.getValue().getOsmLightId())
                .collect(Collectors.joining("\n")));
    }
}
