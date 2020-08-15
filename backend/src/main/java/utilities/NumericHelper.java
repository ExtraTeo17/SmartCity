package utilities;

import org.jetbrains.annotations.Contract;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public class NumericHelper {
    public static final double EARTH_RADIUS = 6364.917;
    public static final double METERS_PER_DEGREE = EARTH_RADIUS * 2 * Math.PI / 360.0 * 1000;

    public static double getEuclideanDistance(GeoPosition posA, GeoPosition posB) {
        return getEuclideanDistance(posA.getLatitude(), posB.getLatitude(),
                posA.getLongitude(), posB.getLongitude());
    }

    @Contract(pure = true)
    public static double getEuclideanDistance(double x1, double x2, double y1, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    /**
     * @return cosine calculated from the Law of Cosines in triangle
     */
    @Contract(pure = true)
    public static double getCosineInTriangle(double a, double b, double c) {
        return ((a * a) + (b * b) - (c * c)) / (2 * a * b);
    }

    /**
     * @param radius DEGREE based radius
     */
    @Contract(pure = true)
    public static boolean isInCircle(Point point, Point center, double radius) {
        return isInCircle(point.x, point.y, center.x, center.y, radius);
    }

    /**
     * @param radius DEGREE based radius
     */
    @Contract(pure = true)
    public static boolean isInCircle(double xPoint, double yPoint,
                                     double xCenter, double yCenter,
                                     double radius) {
        return (xPoint - xCenter) * (xPoint - xCenter) +
                (yPoint - yCenter) * (yPoint - yCenter)
                < (radius * radius);
    }

    /**
     * @param radius METER based radius
     */
    @Contract(pure = true)
    public static boolean isInCircle(Point point, Point center, int radius) {
        return isInCircle(point.x, point.y, center.x, center.y, radius / METERS_PER_DEGREE);
    }

    /**
     * @param radius METER based radius
     */
    @Contract(pure = true)
    public static boolean isInCircle(double xPoint, double yPoint,
                                     double xCenter, double yCenter,
                                     int radius) {
        return isInCircle(xPoint, yPoint, xCenter, yCenter, radius / METERS_PER_DEGREE);
    }

    @Contract(pure = true)
    public static double calculateAverage(List<Double> doubles) {
        return doubles.stream().mapToDouble(NumericHelper::unbox).average().orElse(0);
    }

    @Contract(value = "!null -> param1", pure = true)
    public static double unbox(Double d) {
        return d == null ? 0 : d;
    }

    @Contract(pure = true)
    public static double parseDouble(String val) {
        return val == null || val.isEmpty() ? 0.0 : Double.parseDouble(val);
    }
}
