package smartcity.lights.core;

import agents.utilities.LightColor;
import gui.CustomWaypointRenderer;
import org.javatuples.Pair;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import routing.LightManagerNode;
import routing.core.IGeoPosition;
import smartcity.MasterAgent;
import smartcity.stations.ArrivalInfo;

import java.time.LocalDateTime;
import java.util.*;

public class Light implements IGeoPosition {
    private final double lat;
    private final double lng;
    private LightColor carLightColor;
    private final long adjacentOsmWayId;
    private final String adjacentCrossingOsmId1;
    private final String adjacentCrossingOsmId2;
    private final long osmId;

    private final Map<String, LocalDateTime> farAwayCarMap = new HashMap<>();
    private final Map<String, LocalDateTime> farAwayPedestrianMap = new HashMap<>();
    final Queue<String> carQueue = new LinkedList<>();
    final Queue<String> pedestrianQueue = new LinkedList<>();

    public Light(LightInfo info, LightColor color, int managerId) {
        this.osmId = info.osmLightId;
        this.lat = info.position.getLat();
        this.lng = info.position.getLng();
        this.adjacentOsmWayId = info.adjacentOsmWayId;
        this.adjacentCrossingOsmId1 = info.adjacentCrossingOsmId1;
        this.adjacentCrossingOsmId2 = info.adjacentCrossingOsmId2;
        this.carLightColor = color;
        addHashMapsEntries(managerId);
    }

    @Override
    public double getLat() {
        return lat;
    }

    @Override
    public double getLng() {
        return lng;
    }

    long getAdjacentWayId() {
        return adjacentOsmWayId;
    }

    private void addHashMapsEntries(int managerId) {
        final LightManagerNode lightManagerNode = new LightManagerNode(lat, lng,
                adjacentOsmWayId,
                adjacentCrossingOsmId1 != null ? Long.parseLong(adjacentCrossingOsmId1) : 0,
                adjacentCrossingOsmId2 != null ? Long.parseLong(adjacentCrossingOsmId2) : 0,
                managerId);
        MasterAgent.wayIdLightIdToLightManagerNode.put(Pair.with(adjacentOsmWayId, osmId), lightManagerNode);
        if (adjacentCrossingOsmId1 != null) {
            MasterAgent.crossingOsmIdToLightManagerNode.put(Long.parseLong(adjacentCrossingOsmId1), lightManagerNode);
        }
        if (adjacentCrossingOsmId2 != null) {
            MasterAgent.crossingOsmIdToLightManagerNode.put(Long.parseLong(adjacentCrossingOsmId2), lightManagerNode);
        }
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

    boolean isGreen() {
        return carLightColor == LightColor.GREEN;
    }

    void draw(Collection<Waypoint> lightSet, WaypointPainter<Waypoint> painter) {
        lightSet.add(new DefaultWaypoint(lat, lng));
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

    void addCarToQueue(String agentName) {
        carQueue.add(agentName);
    }

    void removeCarFromFarAwayQueue(String agentName) {
        farAwayCarMap.remove(agentName);
    }

    void removeCarFromQueue() {
        if (carQueue.size() != 0) {
            carQueue.remove();
        }
    }

    void addPedestrianToFarAwayQueue(ArrivalInfo arrivalInfo) {
        farAwayPedestrianMap.put(arrivalInfo.agentName, arrivalInfo.arrivalTime);
    }

    void addPedestrianToQueue(String pedestrianName) {
        pedestrianQueue.add(pedestrianName);
    }

    void removePedestrianFromFarAwayQueue(String pedestrianName) {
        farAwayPedestrianMap.remove(pedestrianName);
    }

    void removePedestrianFromQueue() {
        if (pedestrianQueue.size() != 0) {
            pedestrianQueue.remove();
        }
    }
}
