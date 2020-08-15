package smartcity.lights;


import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import osmproxy.elements.OSMWaypoint;
import routing.IGeoPosition;

public class LightInfo implements IGeoPosition {
    private static final double DISTANCE_THRESHOLD = 0.00008;
    private final String osmLightId;
    private final String adjacentOsmWayId;
    private IGeoPosition position;
    private String adjacentCrossingOsmId1;
    private String adjacentCrossingOsmId2;

    public LightInfo(OSMWay adjacentOsmWay, OSMNode centerCrossroadNode,
                     double distToCrossroad) {
        this.adjacentOsmWayId = Long.toString(adjacentOsmWay.getId());
        this.osmLightId = Long.toString(centerCrossroadNode.getId());
        fillLightPositionAndCrossings(adjacentOsmWay);
        if (distToCrossroad > DISTANCE_THRESHOLD) {
            position = position.midpoint(centerCrossroadNode);
            position = position.midpoint(centerCrossroadNode);
        }
    }

    private void fillLightPositionAndCrossings(OSMWay adjacentOsmWay) {
        OSMWaypoint secondWaypoint = null;
        OSMWaypoint thirdWaypoint = null;
        switch (adjacentOsmWay.getLightOrientation()) {
            case LIGHT_AT_ENTRY -> {
                secondWaypoint = adjacentOsmWay.getWaypoint(1);
                thirdWaypoint = adjacentOsmWay.getWaypointCount() > 2 ? adjacentOsmWay.getWaypoint(2) : null;
            }
            case LIGHT_AT_EXIT -> {
                secondWaypoint = adjacentOsmWay.getWaypoint(adjacentOsmWay.getWaypointCount() - 2);
                thirdWaypoint = adjacentOsmWay.getWaypointCount() > 2 ?
                        adjacentOsmWay.getWaypoint(adjacentOsmWay.getWaypointCount() - 3) : null;
            }
        }
        position = secondWaypoint;
        adjacentCrossingOsmId1 = secondWaypoint.getOsmNodeRef();
        adjacentCrossingOsmId2 = thirdWaypoint != null ? thirdWaypoint.getOsmNodeRef() : null;
    }

    public String getOsmLightId() {
        return osmLightId;
    }

    @Override
    public double getLat() {
        return position.getLat();
    }

    @Override
    public double getLng() {
        return position.getLng();
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
