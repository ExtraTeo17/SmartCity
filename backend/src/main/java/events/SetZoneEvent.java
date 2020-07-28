package events;

public class SetZoneEvent {
    public final double latitude;
    public final double longitude;
    public final double radius;

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
}
