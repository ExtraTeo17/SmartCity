package routing;

import com.google.inject.Inject;
import routing.abstractions.IRoutingHelper;
import routing.core.IGeoPosition;
import routing.core.IZone;
import routing.core.Position;
import utilities.Siblings;

import java.util.Random;

public class RoutingHelper implements IRoutingHelper {
    private final Random random;

    @Inject
    public RoutingHelper(Random random) {
        this.random = random;
    }

    @Override
    public Siblings<IGeoPosition> getRandomPositions(IZone zone) {
        var zoneCenter = zone.getCenter();
        var geoPosInZoneCircle = generateRandomOffset(zone.getRadius(), zoneCenter.getLat());

        var posA = zoneCenter.sum(geoPosInZoneCircle);
        var posB = zoneCenter.diff(geoPosInZoneCircle);

        return Siblings.of(posA, posB);
    }

    // https://gis.stackexchange.com/a/25883/172684
    @Override
    public IGeoPosition generateRandomOffset(int radius, double lat0) {
        var randU = random.nextDouble();
        var randV = random.nextDouble();

        var degRadius = radius / RoutingConstants.METERS_PER_DEGREE;
        var w = degRadius * Math.sqrt(randU);
        var t = 2 * Math.PI * randV;

        double lng = w * Math.cos(t);
        double lat = w * Math.sin(t);

        // Adjusting the x-coordinate for the shrinking of the east-west distances
        lng = lng / Math.cos(Math.toRadians(lat0));

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
