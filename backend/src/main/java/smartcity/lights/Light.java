package smartcity.lights;

import agents.utilities.LightColor;
import gui.CustomWaypointRenderer;
import org.javatuples.Pair;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.w3c.dom.Node;
import routing.LightManagerNode;
import routing.core.IGeoPosition;
import smartcity.MasterAgent;

import java.time.Instant;
import java.util.*;

public class Light implements IGeoPosition {
    private static final String OSM_LIGHT_ID = "light";
    private static final String WAY_ID = "way";
    private static final String LAT = "lat";
    private static final String LON = "lon";

    private final double lat;
    private final double lng;
    Map<String, Instant> farAwayCarMap = new HashMap<>();
    Map<String, Instant> farAwayPedestrianMap = new HashMap<>();
    Queue<String> carQueue = new LinkedList<>();
    Queue<String> pedestrianQueue = new LinkedList<>();
    private LightColor carLightColor;
    private final long adjacentOsmWayId;
    private String adjacentCrossingOsmId1;
    private String adjacentCrossingOsmId2;
    private final long osmId;

    Light(Node node, LightColor color, int managerId) {
        this.carLightColor = color;
        osmId = Long.parseLong(node.getAttributes().getNamedItem(Light.OSM_LIGHT_ID).getNodeValue());
        lat = Double.parseDouble((node.getAttributes().getNamedItem(Light.LAT).getNodeValue()));
        lng = Double.parseDouble((node.getAttributes().getNamedItem(Light.LON).getNodeValue()));
        adjacentOsmWayId = Long.parseLong((node.getAttributes().getNamedItem(Light.WAY_ID).getNodeValue())); // TODO: Retrieve crossings!
        addHashMapsEntries(managerId);
    }

    Light(LightInfo info, LightColor color, int managerId) {
        this.carLightColor = color;
        this.osmId = Long.parseLong(info.getOsmLightId());
        this.lat = info.getLat();
        this.lng = info.getLng();
        adjacentOsmWayId = Long.parseLong(info.getAdjacentOsmWayId());
        adjacentCrossingOsmId1 = info.getAdjacentCrossingOsmId1();
        adjacentCrossingOsmId2 = info.getAdjacentCrossingOsmId2();
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

    public long getAdjacentOSMWayId() {
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

    void addCarToFarAwayQueue(String carName, Instant journeyTime) {
        farAwayCarMap.put(carName, journeyTime);
    }

    void addCarToQueue(String carName) {
        carQueue.add(carName);
    }

    void removeCarFromFarAwayQueue(String carName) {
        farAwayCarMap.remove(carName);
    }

    void removeCarFromQueue() {
        if (carQueue.size() != 0) {
            carQueue.remove();
        }
    }

    void addPedestrianToFarAwayQueue(String pedestrianName, Instant journeyTime) {
        farAwayPedestrianMap.put(pedestrianName, journeyTime);
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
