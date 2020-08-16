package utilities;

import org.jetbrains.annotations.Contract;
import routing.IGeoPosition;

import java.util.List;

public final class NumericHelper {
    public static final double EARTH_RADIUS = 6364.917;
    public static final double METERS_PER_DEGREE = EARTH_RADIUS * 2 * Math.PI / 360.0 * 1000;

    /**
     * @return cosine in radians, calculated from the Law of Cosines in triangle
     */
    @Contract(pure = true)
    public static double getCosineInTriangle(double a, double b, double c) {
        return ((a * a) + (b * b) - (c * c)) / (2 * a * b);
    }

    /**
     * @param radius METER based radius
     */
    @Contract(pure = true)
    public static boolean isInCircle(IGeoPosition point, IGeoPosition center, int radius) {
        var degreeRadius = radius / METERS_PER_DEGREE;
        return center.diff(point).squaredSum() < degreeRadius * degreeRadius;
    }

    @Contract(pure = true)
    public static double calculateAverage(List<Double> doubles) {
        return doubles.stream().mapToDouble(NumericHelper::unbox).average().orElse(0);
    }

    @Contract(value = "!null -> param1", pure = true)
    public static double unbox(Double d) {
        return d == null ? 0 : d;
    }
}
