package smartcity.lights.core;

import agents.utilities.LightColor;
import gui.CustomWaypointRenderer;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import routing.core.Position;
import smartcity.stations.ArrivalInfo;

import java.time.LocalDateTime;
import java.util.*;

public class Light extends Position {
    private LightColor carLightColor;
    private final long adjacentOsmWayId;
    private final String adjacentCrossingOsmId1;
    private final String adjacentCrossingOsmId2;
    private final long osmLightId;

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
    }

    public long getOsmLightId() {
        return osmLightId;
    }

    public long getAdjacentWayId() {
        return adjacentOsmWayId;
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

    void draw(Collection<Waypoint> lightSet, WaypointPainter<Waypoint> painter) {
        lightSet.add(new DefaultWaypoint(getLat(), getLng()));
        switch (carLightColor) {
            case RED -> painter.setRenderer(new CustomWaypointRenderer("light_red.png"));
            case YELLOW -> painter.setRenderer(new CustomWaypointRenderer("light_yellow.png"));
            case GREEN -> painter.setRenderer(new CustomWaypointRenderer("light_green.png"));
        }
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
        farAwayCarMap.remove(agentName);
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
}
