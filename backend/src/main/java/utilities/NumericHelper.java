package utilities;

import org.jetbrains.annotations.Contract;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.List;

public class NumericHelper {
    public static double getEuclideanDistance(GeoPosition posA, GeoPosition posB) {
        return getEuclideanDistance(posA.getLatitude(), posB.getLatitude(),
                posA.getLongitude(), posB.getLongitude());
    }

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


    // TODO: Add belongsToCircle with degree-based radius
    public static boolean belongsToCircle(Point point, Point center, double radius) {
        return (point.x - center.x) * (point.x - center.x) +
                (point.y - center.y) * (point.y - center.y)
                < (radius * radius);
    }

    public static double calculateAverage(List<Double> doubles) {
        return doubles.stream().mapToDouble(NumericHelper::unbox).average().orElse(0);
    }

    public static double unbox(Double d) {
        return d == null ? 0 : d;
    }

    public static double parseDouble(String val) {
        return val == null || val.isEmpty() ? 0.0 : Double.parseDouble(val);
    }
}
