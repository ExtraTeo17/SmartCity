package osmproxy.elements;

import org.jxmapviewer.viewer.GeoPosition;
import osmproxy.MapAccessManager;
import utilities.NumericHelper;
import utilities.Point;

public class OSMWaypoint extends GeoPosition {
    private final String osmNodeRef;

    OSMWaypoint(String nodeRef, double lat, double lng) {
        super(lat, lng);
        osmNodeRef = nodeRef;
    }

    public final String getOsmNodeRef() {
        return osmNodeRef;
    }

    @Override
    public final String toString() {
        return super.toString();
    }
}
