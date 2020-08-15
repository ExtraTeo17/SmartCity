package routing;

import org.jxmapviewer.viewer.GeoPosition;
import utilities.NumericHelper;

public interface IGeoPosition {
    double getLat();

    double getLng();

    default IGeoPosition midpoint(IGeoPosition other) {
        return new IGeoPosition() {
            private final double lat = (IGeoPosition.this.getLat() + other.getLat()) / 2.0;
            private final double lng = (IGeoPosition.this.getLng() + other.getLng()) / 2.0;

            @Override
            public double getLat() {
                return lat;
            }

            @Override
            public double getLng() {
                return lng;
            }
        };
    }

    default double distance(IGeoPosition other) {
        return NumericHelper.getEuclideanDistance(getLat(), other.getLat(), getLng(), other.getLng());
    }

    default IGeoPosition sum(IGeoPosition other) {
        return new IGeoPosition() {
            private final double lat = IGeoPosition.this.getLat() + other.getLat();
            private final double lng = IGeoPosition.this.getLng() + other.getLng();

            @Override
            public double getLat() {
                return lat;
            }

            @Override
            public double getLng() {
                return lng;
            }
        };
    }

    default IGeoPosition difference(IGeoPosition other) {
        return new IGeoPosition() {
            private final double lat = IGeoPosition.this.getLat() - other.getLat();
            private final double lng = IGeoPosition.this.getLng() - other.getLng();

            @Override
            public double getLat() {
                return lat;
            }

            @Override
            public double getLng() {
                return lng;
            }
        };
    }

    @Deprecated
    default GeoPosition toMapGeoPosition() {
        return new GeoPosition(getLat(), getLng());
    }
}
