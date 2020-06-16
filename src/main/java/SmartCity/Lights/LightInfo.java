package SmartCity.Lights;

import org.jxmapviewer.viewer.GeoPosition;

import OSMProxy.Elements.OSMNode;
import OSMProxy.Elements.OSMWay;

public class LightInfo {
	
	private static final double DISTANCE_THRESHOLD = 0.00008;
	
	private String osmLightId;
	private GeoPosition position;
	private String adjacentOsmWayId;

	public LightInfo(OSMWay adjacentOsmWay, OSMNode centerCrossroadNode,
			double distToCrossroad) {
		adjacentOsmWayId = Long.toString(adjacentOsmWay.getId());
		this.osmLightId = Long.toString(centerCrossroadNode.getId());
		fillLightPosition(adjacentOsmWay);
		shiftTowardsCrossroad(centerCrossroadNode.getPosition(), distToCrossroad);
	}

	private void fillLightPosition(OSMWay adjacentOsmWay) {
		switch (adjacentOsmWay.getLightOrientation()) {
		case LIGHT_AT_ENTRY:
			position = new GeoPosition(adjacentOsmWay.getWaypoint(0 + 1).getLat(),
					adjacentOsmWay.getWaypoint(0 + 1).getLon());
			break;
		case LIGHT_AT_EXIT:
			position = new GeoPosition(adjacentOsmWay.getWaypoint(adjacentOsmWay.getWaypointCount() - 1 - 1).getLat(),
					adjacentOsmWay.getWaypoint(adjacentOsmWay.getWaypointCount() - 1 - 1).getLon());
			break;
		}
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
}
