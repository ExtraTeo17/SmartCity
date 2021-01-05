package smartcity.config.abstractions;
//TODO:dokumentacja

public interface IGenerationConfigContainer {

    boolean shouldGeneratePedestriansAndBuses();

    void setGeneratePedestriansAndBuses(boolean value);

    boolean shouldGenerateBatchesForCars();

    void setShouldGenerateBatchesForCars(boolean generateBatchesForCars);

    boolean shouldUseFixedRoutes();

    void setUseFixedRoutes(boolean value);
}
