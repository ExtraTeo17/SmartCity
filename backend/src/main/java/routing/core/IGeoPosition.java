package routing.core;

import org.jetbrains.annotations.NotNull;
import routing.RoutingHelper;
import utilities.NumericHelper;

public interface IGeoPosition extends Comparable<IGeoPosition> {
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


    /**
     * @return Distance in meters between two lat-lng points
     */
    default double distance(IGeoPosition other) {
        return RoutingHelper.getDistance(this, other);
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

    default IGeoPosition multiply(double multipler) {
        return new IGeoPosition() {
            private final double lat = IGeoPosition.this.getLat() * multipler;
            private final double lng = IGeoPosition.this.getLng() * multipler;

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
        var a = this.distance(posA);
        var b = this.distance(posB);
        var c = posA.distance(posB);
        return NumericHelper.getCosineInTriangle(a, b, c);
    }

    default IGeoPosition toRadians() {
        return new IGeoPosition() {
            private final double lat = Math.toRadians(IGeoPosition.this.getLat());
            private final double lng = Math.toRadians(IGeoPosition.this.getLng());

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

    @Override
    default int compareTo(@NotNull IGeoPosition o) {
        var cmp = Double.compare(getLat(), o.getLat());
        return cmp != 0 ? cmp : Double.compare(getLng(), o.getLng());
    }

    default String toText() {
        return "(" + getLat() + ", " + getLng() + ')';
    }
}
