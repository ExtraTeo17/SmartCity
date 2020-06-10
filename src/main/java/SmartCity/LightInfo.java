package SmartCity;

import OSMProxy.Elements.OSMWay;

public class LightInfo {
	
	private String osmLightId;
	private String lat;
	private String lon;
	private String adjacentOsmWayId;

	public LightInfo(OSMWay adjacentOsmWay, long osmLightId) {
		adjacentOsmWayId = Long.toString(adjacentOsmWay.getId());
		this.osmLightId = Long.toString(osmLightId);
		fillLightPosition(adjacentOsmWay);
	}

	private void fillLightPosition(OSMWay adjacentOsmWay) {
		switch (adjacentOsmWay.getOrientation()) {
		case LIGHT_AT_ENTRY:
			lat = Double.toString(adjacentOsmWay.getWaypoint(0 + 1).getLat());
			lon = Double.toString(adjacentOsmWay.getWaypoint(0 + 1).getLon());
			break;
		case LIGHT_AT_EXIT:
			lat = Double.toString(adjacentOsmWay.getWaypoint(adjacentOsmWay.getWaypointCount() - 1 - 1).getLat());
			lon = Double.toString(adjacentOsmWay.getWaypoint(adjacentOsmWay.getWaypointCount() - 1 - 1).getLon());
			break;
		}
	}

	public String getOsmLightId() {
		return osmLightId;
	}

	public String getLat() {
		return lat;
	}

	public String getLon() {
		return lon;
	}

	public String getAdjacentOsmWayId() {
		return adjacentOsmWayId;
	}
}
