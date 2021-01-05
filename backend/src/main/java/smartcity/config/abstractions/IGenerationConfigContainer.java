package smartcity.config.abstractions;

/**
 * Controls generation of objects
 */
public interface IGenerationConfigContainer {

    boolean shouldGeneratePedestriansAndBuses();

    void setGeneratePedestriansAndBuses(boolean value);

    boolean shouldGenerateBatchesForCars();

    void setShouldGenerateBatchesForCars(boolean generateBatchesForCars);

    boolean shouldUseFixedRoutes();

    void setUseFixedRoutes(boolean value);
}
