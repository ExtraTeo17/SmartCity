package smartcity.config.abstractions;

public interface IGenerationConfigContainer {

    boolean shouldGeneratePedestriansAndBuses();

    void setGeneratePedestriansAndBuses(boolean value);

    boolean shouldGenerateBatchesForCars();

    void setGenerateBatchesForCars(boolean generateBatchesForCars);

    boolean shouldUseFixedRoutes();

    void setUseFixedRoutes(boolean value);
}
