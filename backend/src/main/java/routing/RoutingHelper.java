package routing;

import com.google.inject.Inject;
import routing.abstractions.IRoutingHelper;
import routing.core.IGeoPosition;
import routing.core.Position;

import java.util.Random;

public class RoutingHelper implements IRoutingHelper {
    private final Random random;

    @Inject
    public RoutingHelper(Random random) {
        this.random = random;
    }

    @Override
    public IGeoPosition generateRandomOffset(int radius) {
        double angle = random.nextDouble() * Math.PI * 2;
        double lat = Math.sin(angle) * radius * RoutingConstants.DEGREES_PER_METER;
        double lng = Math.cos(angle) * radius * RoutingConstants.DEGREES_PER_METER * Math.cos(lat);
        return Position.of(lat, lng);
    }

    public static double getDistance(IGeoPosition posA, IGeoPosition posB) {
        var delta = posA.diff(posB).toRadians();
        var dLat = delta.getLat() / 2;
        var dLng = delta.getLng() / 2;

        var latPosA = posA.getLat();
        var latPosB = posB.getLat();
        var haversine = Math.sin(dLat) * Math.sin(dLat) +
                Math.cos(Math.toRadians(latPosB)) * Math.cos(Math.toRadians(latPosA)) * Math.sin(dLng) * Math.sin(dLng);
        var dist = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));

        return RoutingConstants.EARTH_RADIUS_METERS * dist;
    }
}
