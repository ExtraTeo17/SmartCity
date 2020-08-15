package smartcity.lights;

import org.jxmapviewer.viewer.GeoPosition;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import osmproxy.elements.OSMWaypoint;

public class LightInfo {
    private static final double DISTANCE_THRESHOLD = 0.00008;

    private final String osmLightId;
    private final String adjacentOsmWayId;
    private GeoPosition position;
    private String adjacentCrossingOsmId1;
    private String adjacentCrossingOsmId2;

    public LightInfo(OSMWay adjacentOsmWay, OSMNode centerCrossroadNode,
                     double distToCrossroad) {
        adjacentOsmWayId = Long.toString(adjacentOsmWay.getId());
        this.osmLightId = Long.toString(centerCrossroadNode.getId());
        fillLightPositionAndCrossings(adjacentOsmWay);
        shiftTowardsCrossroad(centerCrossroadNode.getPosition(), distToCrossroad);
    }

    private void fillLightPositionAndCrossings(OSMWay adjacentOsmWay) {
        OSMWaypoint secondWaypoint = null, thirdWaypoint = null;
        switch (adjacentOsmWay.getLightOrientation()) {
            case LIGHT_AT_ENTRY -> {
                secondWaypoint = adjacentOsmWay.getWaypoint(1);
                thirdWaypoint = adjacentOsmWay.getWaypointCount() > 2 ? adjacentOsmWay.getWaypoint(2) : null;
            }
            case LIGHT_AT_EXIT -> {
                secondWaypoint = adjacentOsmWay.getWaypoint(adjacentOsmWay.getWaypointCount() - 1 - 1);
                thirdWaypoint = adjacentOsmWay.getWaypointCount() > 2 ?
                        adjacentOsmWay.getWaypoint(adjacentOsmWay.getWaypointCount() - 1 - 2) : null;
            }
        }
        position = new GeoPosition(secondWaypoint.getLatitude(), secondWaypoint.getLongitude());
        adjacentCrossingOsmId1 = secondWaypoint.getOsmNodeRef();
        adjacentCrossingOsmId2 = thirdWaypoint != null ? thirdWaypoint.getOsmNodeRef() : null;
    }

    private void shiftTowardsCrossroad(final GeoPosition crossroadPos, final double distToCrossroad) {
        if (distToCrossroad > DISTANCE_THRESHOLD) {
            shiftTowardsCrossroad(crossroadPos);
            shiftTowardsCrossroad(crossroadPos);
        }
    }

    private void shiftTowardsCrossroad(GeoPosition crossroadPos) {
        position = getMiddlePoint(crossroadPos, position);
    }

    private GeoPosition getMiddlePoint(GeoPosition point1, GeoPosition point2) {
        return new GeoPosition((point1.getLatitude() + point2.getLatitude()) / 2,
                (point1.getLongitude() + point2.getLongitude()) / 2);
    }

    public String getOsmLightId() {
        return osmLightId;
    }

    public String getLat() {
        return Double.toString(position.getLatitude());
    }

    public String getLon() {
        return Double.toString(position.getLongitude());
    }

    public String getAdjacentOsmWayId() {
        return adjacentOsmWayId;
    }

    String getAdjacentCrossingOsmId1() {
        return adjacentCrossingOsmId1;
    }

    public String getAdjacentCrossingOsmId2() {
        return adjacentCrossingOsmId2;
    }
}
