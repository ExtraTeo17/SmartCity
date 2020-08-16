package utilities;

import org.jetbrains.annotations.Contract;

import java.util.List;

public final class NumericHelper {
    public static final double EARTH_RADIUS_METERS = 6_378_137;
    public static final double METERS_PER_DEGREE = EARTH_RADIUS_METERS * Math.PI / 180.0;

    /**
     * @return cosine in radians, calculated from the Law of Cosines in triangle
     */
    @Contract(pure = true)
    public static double getCosineInTriangle(double a, double b, double c) {
        return ((a * a) + (b * b) - (c * c)) / (2 * a * b);
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
