package events;

public class SetZoneEvent {
    private final double latitude;
    private final double longitude;
    private final double radius;

    public SetZoneEvent(double latitude, double longitude, double radius) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    @Override
    public String toString() {
        return '(' +
                "latitude: " + latitude +
                ", longitude: " + longitude +
                ", radius: " + radius +
                ')';
    }

    public double getLat() {
        return latitude;
    }

    public double getLng() {
        return longitude;
    }

    public double getRadius() {
        return radius;
    }
}
