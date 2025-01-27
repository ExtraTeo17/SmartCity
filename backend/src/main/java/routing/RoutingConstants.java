package routing;

public class RoutingConstants {
    public static final int STEP_SIZE_METERS = 2;
    public static final int M_MILLISECONDS_TO_KM_HOUR = 3600;
    public static final int STEP_CONSTANT = STEP_SIZE_METERS * M_MILLISECONDS_TO_KM_HOUR;
    // Experimental value: (delay per node)
    public static final double CALCULATION_DELTA_PER_INDEX = 0.02888831;

    public static final double EARTH_RADIUS_METERS = 6_378_137;
    public static final double METERS_PER_DEGREE = EARTH_RADIUS_METERS * Math.PI / 180.0;
    public static final double DEGREES_PER_METER = 1 / METERS_PER_DEGREE;
}
