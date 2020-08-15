package osmproxy.elements;


import routing.Position;

public class OSMWaypoint extends Position {
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
