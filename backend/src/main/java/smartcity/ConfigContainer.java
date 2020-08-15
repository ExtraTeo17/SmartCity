package smartcity;

import routing.*;

public final class ConfigContainer extends ZoneMutator {
    public final boolean USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS = false;
    public final IGeoPosition warsawPos = new Position(52.23682, 21.01681);
    public final int defaultRadius = 600;

    private boolean shouldGeneratePedestriansAndBuses = false;
    private boolean shouldGenerateCars = true;
    private final IZone zone;

    public ConfigContainer() {
        this.zone = new Zone(warsawPos, defaultRadius);
    }

    public boolean shouldGeneratePedestriansAndBuses() {
        return shouldGeneratePedestriansAndBuses;
    }

    public boolean shouldGenerateCars() {
        return shouldGenerateCars;
    }

    public IZone getZone() {
        return zone;
    }

    public void setZone(double lat, double lng, int radius) {
        zone.setZone(mutation, Position.of(lat, lng), radius);
    }

    public void setZone(IGeoPosition pos, int radius) {
        zone.setZone(mutation, pos, radius);
    }
}
