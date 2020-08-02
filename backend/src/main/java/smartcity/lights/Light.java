package smartcity.lights;

import agents.utils.LightColor;
import gui.CustomWaypointRenderer;
import org.javatuples.Pair;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.w3c.dom.Node;
import routing.LightManagerNode;
import smartcity.MasterAgent;

import java.time.Instant;
import java.util.*;

public class Light {
    private static final String OSM_LIGHT_ID = "light";
    private static final String WAY_ID = "way";
    private static final String LAT = "lat";
    private static final String LON = "lon";
    public Map<String, Instant> farAwayCarMap = new HashMap<>();
    public Map<String, Instant> farAwayPedestrianMap = new HashMap<>();
    public Queue<String> carQueue = new LinkedList<>();
    public Queue<String> pedestrianQueue = new LinkedList<>();
    private LightColor carLightColor;
    private final GeoPosition position;
    private final long adjacentOsmWayId;
    private String adjacentCrossingOsmId1;
    private String adjacentCrossingOsmId2;
    private final long osmId;

    public Light(Node node, LightColor color, int managerId) {
        this.carLightColor = color;
        osmId = Long.parseLong(node.getAttributes().getNamedItem(Light.OSM_LIGHT_ID).getNodeValue());
        double lat = Double.parseDouble((node.getAttributes().getNamedItem(Light.LAT).getNodeValue()));
        double lon = Double.parseDouble((node.getAttributes().getNamedItem(Light.LON).getNodeValue()));
        position = new GeoPosition(lat, lon);
        adjacentOsmWayId = Long.parseLong((node.getAttributes().getNamedItem(Light.WAY_ID).getNodeValue())); // TODO: Retrieve crossings!
        addHashMapsEntries(managerId);
    }

    public Light(LightInfo info, LightColor color, int managerId) {
        this.carLightColor = color;
        osmId = Long.parseLong(info.getOsmLightId());
        double lat = Double.parseDouble(info.getLat());
        double lon = Double.parseDouble(info.getLon());
        position = new GeoPosition(lat, lon);
        adjacentOsmWayId = Long.parseLong(info.getAdjacentOsmWayId());
        adjacentCrossingOsmId1 = info.getAdjacentCrossingOsmId1();
        adjacentCrossingOsmId2 = info.getAdjacentCrossingOsmId2();
        addHashMapsEntries(managerId);
    }

    public long getAdjacentOSMWayId() {
        return adjacentOsmWayId;
    }

    private void addHashMapsEntries(long managerId) {
        final LightManagerNode lightManagerNode = new LightManagerNode(position.getLatitude(), position.getLongitude(),
                adjacentOsmWayId, Long.parseLong(adjacentCrossingOsmId1), adjacentCrossingOsmId2 != null ?
                Long.parseLong(adjacentCrossingOsmId2) : null, managerId);
        MasterAgent.wayIdLightIdToLightManagerNode.put(Pair.with(adjacentOsmWayId, osmId), lightManagerNode);
        MasterAgent.crossingOsmIdToLightManagerNode.put(Long.parseLong(adjacentCrossingOsmId1), lightManagerNode);
        if (adjacentCrossingOsmId2 != null) {
            MasterAgent.crossingOsmIdToLightManagerNode.put(Long.parseLong(adjacentCrossingOsmId2), lightManagerNode);
        }
    }

    public GeoPosition getPosition() {
        return position;
    }

    public boolean isGreen() {
        return carLightColor == LightColor.GREEN;
    }

    public void draw(Collection<Waypoint> lightSet, WaypointPainter<Waypoint> painter) {
        lightSet.add(new DefaultWaypoint(position));
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

    public void addCarToFarAwayQueue(String carName, Instant journeyTime) {
        farAwayCarMap.put(carName, journeyTime);
    }

    public void addCarToQueue(String carName) {
        carQueue.add(carName);
    }

    public void removeCarFromFarAwayQueue(String carName) {
        farAwayCarMap.remove(carName);
    }

    public void removeCarFromQueue() {
        if (carQueue.size() != 0) {
            carQueue.remove();
        }
    }

    public void addPedestrianToFarAwayQueue(String pedestrianName, Instant journeyTime) {
        farAwayPedestrianMap.put(pedestrianName, journeyTime);
    }

    public void addPedestrianToQueue(String pedestrianName) {
        pedestrianQueue.add(pedestrianName);
    }

    public void removePedestrianFromFarAwayQueue(String pedestrianName) {
        farAwayPedestrianMap.remove(pedestrianName);
    }

    public void removePedestrianFromQueue() {
        if (pedestrianQueue.size() != 0) {
            pedestrianQueue.remove();
        }
    }
}
