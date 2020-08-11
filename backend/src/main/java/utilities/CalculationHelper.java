package utilities;

import org.jetbrains.annotations.Contract;
import org.jxmapviewer.viewer.GeoPosition;

public class CalculationHelper {
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
}
