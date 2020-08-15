package routing;

import org.jxmapviewer.viewer.GeoPosition;

public class RouteNode extends GeoPosition {
    public RouteNode(double lat, double lng) {
        super(lat, lng);
    }

    public RouteNode(final GeoPosition position) {
        this(position.getLatitude(), position.getLongitude());
    }

    @Override
    public String toString() {
        return "(" + getLatitude() + ", " + getLongitude() + ")\n";
    }
}
