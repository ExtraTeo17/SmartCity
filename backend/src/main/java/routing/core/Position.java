package routing.core;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.Objects;

public class Position implements IGeoPosition {
    private final double lat;
    private final double lng;

    protected Position(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    protected Position(IGeoPosition pos) {
        this.lat = pos.getLat();
        this.lng = pos.getLng();
    }

    @Override
    public double getLat() {
        return lat;
    }

    @Override
    public double getLng() {
        return lng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Position position = (Position) o;
        return Double.compare(position.lat, lat) == 0 &&
                Double.compare(position.lng, lng) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lng);
    }

    @Override
    public String toString() {
        return "[" + lat + ", " + lng + "]";
    }

    public static Position of(GeoPosition pos) {
        return new Position(pos.getLatitude(), pos.getLongitude());
    }

    public static Position of(double lat, double lng) {
        return new Position(lat, lng);
    }

    public static Position of(String lat, String lng) {
        return new Position(Double.parseDouble(lat), Double.parseDouble(lng));
    }
}
