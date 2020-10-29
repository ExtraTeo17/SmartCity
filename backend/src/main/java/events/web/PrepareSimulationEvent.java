package events.web;

import routing.core.IZone;
import routing.core.Zone;

public class PrepareSimulationEvent {
    public final IZone zone;
    public final boolean shouldGeneratePedestriansAndBuses;

    public PrepareSimulationEvent(double latitude, double longitude, double radius,
                                  boolean shouldGeneratePedestriansAndBuses) {
        this.zone = Zone.of(latitude, longitude, (int) radius);
        this.shouldGeneratePedestriansAndBuses = shouldGeneratePedestriansAndBuses;
    }

    public PrepareSimulationEvent(IZone zone,
                                  boolean shouldGeneratePedestriansAndBuses) {
        this.zone = zone;
        this.shouldGeneratePedestriansAndBuses = shouldGeneratePedestriansAndBuses;
    }


    @Override
    public String toString() {
        return zone.toString();
    }
}
