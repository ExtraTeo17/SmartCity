package routing;

import utilities.NumericHelper;

import java.util.Objects;

public class Zone implements IZone {
    private IGeoPosition center;
    private int radius;

    public Zone(IGeoPosition center, int radius) {
        this.center = center;
        this.radius = radius;
    }

    public Zone(double lat, double lng, int radius) {
        this.center = Position.of(lat, lng);
        this.radius = radius;
    }

    @Override
    public IGeoPosition getCenter() {
        return center;
    }

    @Override
    public int getRadius() {
        return radius;
    }

    @Override
    public void setZone(ZoneMutator.Mutation mutation, IGeoPosition pos, int radius) {
        Objects.requireNonNull(mutation);
        this.center = pos;
        this.radius = radius;
    }

    @SuppressWarnings("FeatureEnvy")
    // source: https://stackoverflow.com/a/27943/6841224
    @Override
    public boolean contains(IGeoPosition pos) {
        var delta = center.diff(pos).toRadians();
        var dLat = delta.getLat() / 2;
        var dLng = delta.getLng() / 2;

        var latPos = pos.getLat();
        var latCenter = center.getLat();
        var haversine = Math.sin(dLat) * Math.sin(dLat) +
                Math.cos(Math.toRadians(latPos)) * Math.cos(Math.toRadians(latCenter)) * Math.sin(dLng) * Math.sin(dLng);
        var dist = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
        var distMeters = NumericHelper.EARTH_RADIUS_METERS * dist;

        return distMeters <= radius;
    }
}
