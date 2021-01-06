package smartcity.config.abstractions;

import routing.core.IGeoPosition;
import routing.core.IZone;


/**
 * Used to modify {@link IZone} fields values.
 * There can be only one IZoneMutator.
 */
public interface IZoneMutator {
    default void setZone(double lat, double lng, int radius) {
        setZone(new IGeoPosition() {
            @Override
            public double getLat() {
                return lat;
            }

            @Override
            public double getLng() {
                return lng;
            }
        }, radius);
    }

    default void setZone(IZone newZone) {
        setZone(newZone.getCenter(), newZone.getRadius());
    }

    void setZone(IGeoPosition pos, int radius);
}
