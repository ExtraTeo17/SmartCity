package utilities;

import org.jxmapviewer.viewer.GeoPosition;

public class Point {
    public final double x;
    public final double y;

    private Point(double first, double second) {
        this.x = first;
        this.y = second;
    }

    private Point(double first) {
        this(first, 0.0);
    }

    public static Point of(double x) {
        return new Point(x, 0.0);
    }

    public static Point of(double x, double y) {
        return new Point(x, y);
    }

    public static Point of(GeoPosition pos) {
        return new Point(pos.getLatitude(), pos.getLongitude());
    }

    public static Point of(String x, String y) {
        return new Point(NumericHelper.parseDouble(x), NumericHelper.parseDouble(y));
    }
}
