package routing;

import utilities.NumericHelper;

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
        this.center = pos;
        this.radius = radius;
    }

    // TODO: Move computation here or add some utilities for difference in pos
    @Override
    public boolean isInZone(IGeoPosition pos) {
        return NumericHelper.isInCircle(pos, center, radius);
    }
}
