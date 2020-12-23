package events.web;

import routing.core.IZone;
import routing.core.Zone;

/**
 * Prepares simulation , by gathering information from GUI
 */
public class PrepareSimulationEvent {
    public final IZone zone;
    public final boolean shouldGeneratePedestriansAndBuses;

    /**
     * @param latitude                          coordinates of simulation area center point
     * @param longitude                         coordinates of simulation area center point
     * @param radius                            radius of simulation zone
     * @param shouldGeneratePedestriansAndBuses decision variable
     */
    public PrepareSimulationEvent(double latitude, double longitude, double radius,
                                  boolean shouldGeneratePedestriansAndBuses) {
        this.zone = Zone.of(latitude, longitude, (int) radius);
        this.shouldGeneratePedestriansAndBuses = shouldGeneratePedestriansAndBuses;
    }

    /**
     * @param zone                              simulation zone
     * @param shouldGeneratePedestriansAndBuses decision variable
     */
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
