package osmproxy.elements;


import routing.core.Position;

import java.util.Objects;

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
        return osmNodeRef + " " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        OSMWaypoint that = (OSMWaypoint) o;
        return osmNodeRef.equals(that.osmNodeRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), osmNodeRef);
    }
}
