package routing.core;

import smartcity.config.ConfigMutator;

import java.util.Objects;
//TODO:dokumentacja

public interface IZone {
    IGeoPosition getCenter();

    int getRadius();

    default void set(ConfigMutator.Mutation mutation, IGeoPosition pos, int radius) {
        Objects.requireNonNull(mutation);
    }

    default void set(ConfigMutator.Mutation mutation, IZone other) {
        Objects.requireNonNull(mutation);
        set(mutation, other.getCenter(), other.getRadius());
    }

    default boolean contains(IGeoPosition pos) {
        return contains(pos, 0);
    }

    boolean contains(IGeoPosition pos, int radiusTolerance);
}
