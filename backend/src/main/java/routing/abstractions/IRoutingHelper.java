package routing.abstractions;

import routing.core.IGeoPosition;
import routing.core.IZone;
import utilities.Siblings;

public interface IRoutingHelper {
    Siblings<IGeoPosition> getRandomPositions(IZone zone);

    /**
     * @param radius radius for offset in meters
     * @return random lat/lng in degrees
     */
    IGeoPosition generateRandomOffset(int radius, double lat0);
}
