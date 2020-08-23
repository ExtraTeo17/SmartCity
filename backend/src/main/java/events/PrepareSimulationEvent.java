package events;

import routing.core.IZone;
import routing.core.Zone;

public class PrepareSimulationEvent {
    public final IZone zone;

    public PrepareSimulationEvent(double latitude, double longitude, double radius) {
        this.zone = Zone.of(latitude, longitude, (int) radius);
    }

    public PrepareSimulationEvent(IZone zone) {
        this.zone = zone;
    }

    @Override
    public String toString() {
        return zone.toString();
    }
}
