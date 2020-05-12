package Routing;

public class RouteNode {

	private double lat;
	private double lon;
	private long osmWayId;
	
	public RouteNode(double lat, double lon, long osmWayId) {
		this.lat = lat;
		this.lon = lon;
		this.osmWayId = osmWayId;
	}

	public double getLatitude() {
		return lat;
	}

	public double getLongitude() {
		return lon;
	}

	public long getOsmWayId() {
		return osmWayId;
	}
}
