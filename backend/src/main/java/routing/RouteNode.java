package routing;

import org.jxmapviewer.viewer.GeoPosition;

public class RouteNode {
    private final double lat;
    private final double lon;

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
        return "(" + lat + ", " + lon + ")\n";
    }
}
