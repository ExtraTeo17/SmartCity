package Routing;

import org.jxmapviewer.viewer.GeoPosition;

public class RouteNode {

	private double lat;
	private double lon;
	
	public RouteNode(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}
	
	public RouteNode(final GeoPosition position) {
		this.lat = position.getLatitude();
		this.lon = position.getLongitude();
	}

	public double getLatitude() {
		return lat;
	}

	public double getLongitude() {
		return lon;
	}
	
	public final GeoPosition getPosition() {
		return new GeoPosition(getLatitude(), getLongitude());
	}

	@Override
	public String toString() {
		return "(" + Double.toString(lat) + ", " + Double.toString(lon) + ")\n";
	}
}
