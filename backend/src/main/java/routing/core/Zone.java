package routing.core;

import routing.RoutingHelper;
import smartcity.config.ConfigMutator;

import java.util.Objects;

public class Zone implements IZone {
    private IGeoPosition center;
    private int radius;

    Zone(IGeoPosition center, int radius) {
        this.center = center;
        this.radius = radius;
    }

    Zone(double lat, double lng, int radius) {
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
    public void set(ConfigMutator.Mutation mutation, IGeoPosition pos, int radius) {
        Objects.requireNonNull(mutation);
        this.center = pos;
        this.radius = radius;
    }

    @SuppressWarnings("FeatureEnvy")
    // source: https://stackoverflow.com/a/27943/6841224
    @Override
    public boolean contains(IGeoPosition pos, int radiusTolerance) {
        return RoutingHelper.getDistance(center, pos) <= radius + radiusTolerance;
    }

    @Override
    public String toString() {
        return "(" + center + ", " + radius + ')';
    }

    public static Zone of(IGeoPosition center, int radius) {
        return new Zone(center, radius);
    }

    public static Zone of(double lat, double lng, int radius) {
        return new Zone(lat, lng, radius);
    }
}
