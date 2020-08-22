package events;

public class SetZoneEvent {
    public final double lat;
    public final double lng;
    public final double radius;

    public SetZoneEvent(double latitude, double longitude, double radius) {
        this.lat = latitude;
        this.lng = longitude;
        this.radius = radius;
    }

    @Override
    public String toString() {
        return '(' +
                "latitude: " + lat +
                ", longitude: " + lng +
                ", radius: " + radius +
                ')';
    }
}
