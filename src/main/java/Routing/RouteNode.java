package Routing;

public class RouteNode {

	public double lat, lon;
	public long osmWayId;
	
	public RouteNode(double lat, double lon, long osmWayId) {
		this.lat = lat;
		this.lon = lon;
		this.osmWayId = osmWayId;
	}
}
