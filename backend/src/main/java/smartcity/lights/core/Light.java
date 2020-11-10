package smartcity.lights.core;

import org.jetbrains.annotations.NotNull;
import routing.core.Position;
import smartcity.lights.LightColor;
import smartcity.lights.OptimizationResult;
import smartcity.stations.ArrivalInfo;

import java.time.LocalDateTime;
import java.util.*;

public class Light extends Position {
    private static final int TRAFFIC_JAM_THRESHOLD = 2;
    private LightColor carLightColor;
    private final long adjacentOsmWayId;
    private final String adjacentCrossingOsmId1;
    private final String adjacentCrossingOsmId2;
    private final long osmLightId;
    private boolean trafficJamOngoing;

    private final Map<String, LocalDateTime> farAwayCarMap = new HashMap<>();
    private final Map<String, LocalDateTime> farAwayPedestrianMap = new HashMap<>();
    final Queue<String> carQueue = new LinkedList<>();
    final Queue<String> pedestrianQueue = new LinkedList<>();

    public Light(LightInfo info, LightColor color) {
        super(info.position);
        this.osmLightId = info.osmLightId;
        this.adjacentOsmWayId = info.adjacentOsmWayId;
        this.adjacentCrossingOsmId1 = info.adjacentCrossingOsmId1;
        this.adjacentCrossingOsmId2 = info.adjacentCrossingOsmId2;
        this.carLightColor = color;
        this.trafficJamOngoing = false;
    }

    public long getOsmLightId() {
        return osmLightId;
    }

    public long getAdjacentWayId() {
        return adjacentOsmWayId;
    }

    public long uniqueId() {
        return super.longHash();
    }

    @NotNull
    public String getAdjacentCrossingOsmId1() {
        return adjacentCrossingOsmId1;
    }

    public String getAdjacentCrossingOsmId2() {
        return adjacentCrossingOsmId2;
    }

    public int getGreenGroupSize() {
        if (isGreen()) {
            return carQueue.size();
        }

        return pedestrianQueue.size();
    }

    public int getRedGroupSize() {
        if (isGreen()) {
            return pedestrianQueue.size();
        }

        return carQueue.size();
    }

    public Collection<LocalDateTime> getFarAwayTimeCollection() {
        if (isGreen()) {
            return farAwayCarMap.values();
        }

        return farAwayPedestrianMap.values();
    }

    public boolean isGreen() {
        return carLightColor == LightColor.GREEN;
    }

    public void switchLight() {
        if (carLightColor == LightColor.RED) {
            carLightColor = LightColor.GREEN;
        }
        else if (carLightColor == LightColor.GREEN) {
            carLightColor = LightColor.RED;
        }
    }

    void addCarToFarAwayQueue(ArrivalInfo arrivalInfo) {
        farAwayCarMap.put(arrivalInfo.agentName, arrivalInfo.arrivalTime);
    }

    void removeCarFromFarAwayQueue(String agentName) {
        if (!farAwayCarMap.containsKey(agentName)) {
            farAwayCarMap.remove(agentName);
        }
    }

    void addPedestrianToFarAwayQueue(ArrivalInfo arrivalInfo) {
        farAwayPedestrianMap.put(arrivalInfo.agentName, arrivalInfo.arrivalTime);
    }

    void removePedestrianFromFarAwayQueue(String pedestrianName) {
        farAwayPedestrianMap.remove(pedestrianName);
    }

    void addCarToQueue(String agentName) {
        carQueue.add(agentName);
    }

    void removeCarFromQueue() {
        if (carQueue.size() != 0) {
            carQueue.remove();
        }
    }

    void addPedestrianToQueue(String pedestrianName) {
        pedestrianQueue.add(pedestrianName);
    }

    void removePedestrianFromQueue() {
        if (pedestrianQueue.size() != 0) {
            pedestrianQueue.remove();
        }
    }

    private boolean trafficJamEmerged() {
        if (carQueue.size() >= TRAFFIC_JAM_THRESHOLD && !trafficJamOngoing) {
            trafficJamOngoing = true;
            return true;
        }
        return false;
    }

    private boolean trafficJamDisappeared() {
        if (carQueue.size() <= TRAFFIC_JAM_THRESHOLD && trafficJamOngoing) {
            trafficJamOngoing = false;
            return true;
        }
        return false;
    }

    final void checkForTrafficJams(final OptimizationResult result) {
        if (trafficJamEmerged()) {
            result.setShouldNotifyCarAboutStartOfTrafficJamOnThisLight(this, carQueue.size(), getOsmLightId());
            result.setCarStuckInJam(carQueue.peek());
        }
        else if (trafficJamDisappeared()) {
            result.setShouldNotifyCarAboutEndOfTrafficJamOnThisLight(getLat(), getLng(), getOsmLightId());
        }
    }
}
