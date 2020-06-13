package OSMProxy.Elements;

import org.jxmapviewer.viewer.GeoPosition;

public class OSMWaypoint {
	
	private final String osmNodeRef;
	private final GeoPosition geoPos;
	
	public OSMWaypoint(String nodeRef, double lat, double lng) {
		osmNodeRef = nodeRef;
		geoPos = new GeoPosition(lat, lng);
	}
	
	public final String getOsmNodeRef() {
		return osmNodeRef;
	}
	
	public final double getLat() {
		return geoPos.getLatitude();
	}
	
	public final double getLon() {
		return geoPos.getLongitude();
	}
	
	public final GeoPosition getPosition() {
		return geoPos;
	}
}