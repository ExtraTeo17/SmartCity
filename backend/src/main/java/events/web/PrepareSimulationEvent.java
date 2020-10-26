package events.web;

import routing.core.IZone;
import routing.core.Zone;

public class PrepareSimulationEvent {
    public final IZone zone;
    public final boolean shouldGeneratePedestriansAndBuses;
    public final int pedestriansLimit;
    public final int testPedestrianId;

    public PrepareSimulationEvent(double latitude, double longitude, double radius,
                                  boolean shouldGeneratePedestriansAndBuses,
                                  int pedestriansLimit,
                                  int testPedestrianId) {
        this.zone = Zone.of(latitude, longitude, (int) radius);
        this.shouldGeneratePedestriansAndBuses = shouldGeneratePedestriansAndBuses;
        this.pedestriansLimit = pedestriansLimit;
        this.testPedestrianId = testPedestrianId;
    }

    public PrepareSimulationEvent(IZone zone,
                                  boolean shouldGeneratePedestriansAndBuses,
                                  int pedestriansLimit,
                                  int testPedestrianId) {
        this.zone = zone;
        this.shouldGeneratePedestriansAndBuses = shouldGeneratePedestriansAndBuses;
        this.pedestriansLimit = pedestriansLimit;
        this.testPedestrianId = testPedestrianId;
    }


    @Override
    public String toString() {
        return zone.toString();
    }
}
