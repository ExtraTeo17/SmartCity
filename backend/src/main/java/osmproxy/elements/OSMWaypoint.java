package osmproxy.elements;

import org.jxmapviewer.viewer.GeoPosition;
import osmproxy.MapAccessManager;
import utilities.NumericHelper;
import utilities.Point;

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

    // TODO: Delete
    boolean containedInCircle(int radius, double middleLat, double middleLon) {
        return NumericHelper.belongsToCircle(Point.of(geoPos), Point.of(middleLat, middleLon),
                radius / MapAccessManager.METERS_PER_DEGREE);
    }
}
