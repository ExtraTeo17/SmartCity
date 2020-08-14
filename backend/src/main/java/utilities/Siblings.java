package utilities;

import org.jxmapviewer.viewer.GeoPosition;

public class Siblings<T> {
    public final T first;
    public final T second;

    public Siblings(T first, T second) {
        this.first = first;
        this.second = second;
    }

    public Siblings(T first) {
        this(first, null);
    }

    public boolean isSecondPresent() {
        return second != null;
    }

    public static Siblings<Double> of(GeoPosition pos) {
        return new Siblings<>(pos.getLatitude(), pos.getLongitude());
    }
}