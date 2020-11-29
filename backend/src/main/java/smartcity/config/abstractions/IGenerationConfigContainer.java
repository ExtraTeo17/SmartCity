package smartcity.config.abstractions;

public interface IGenerationConfigContainer {
    boolean shouldUseFixedRoutes();

    void setUseFixedRoutes(boolean value);
}
