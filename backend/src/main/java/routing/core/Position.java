package routing.core;

import org.jxmapviewer.viewer.GeoPosition;
import utilities.ForSerialization;

import java.io.Serializable;
import java.util.Objects;

public class Position implements IGeoPosition, Serializable {
    public static final int precisionDigits = 8;
    private final double lat;
    private final double lng;

    @ForSerialization
    protected Position() {
        lat = 0;
        lng = 0;
    }

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

    public long longHash() {
        return longHash(this.lat, this.lng);
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

    public static long longHash(double lat, double lng) {
        var precisionShift = Math.pow(20, precisionDigits);

        // Cantor pairing function :)
        return (long) (precisionShift * ((lat + lng) / 2 * (lat + lng + 1) + lng));
    }
}
