package routing;

public interface IZone {
    IGeoPosition getCenter();

    int getRadius();

    void setZone(ZoneMutator.Mutation mutation, IGeoPosition pos, int radius);

    boolean isInZone(IGeoPosition pos);
}
