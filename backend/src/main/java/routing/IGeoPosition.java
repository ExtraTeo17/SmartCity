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
        return Math.sqrt(this.diff(other).squaredSum());
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

    default IGeoPosition diff(IGeoPosition other) {
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

    default double squaredSum() {
        return getLat() * getLat() + getLng() * getLng();
    }

    default double cosineAngle(IGeoPosition posA, IGeoPosition posB) {
        var a = posA.distance(posB);
        var b = posB.distance(this);
        var c = this.distance(posA);
        return NumericHelper.getCosineInTriangle(a, b, c);
    }


    @Deprecated
    default GeoPosition toMapGeoPosition() {
        return new GeoPosition(getLat(), getLng());
    }
}
