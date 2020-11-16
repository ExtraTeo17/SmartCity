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
}
