package smartcity.config.abstractions;

/**
 * Contains all change-transport related configuration properties.
 */
public interface IChangeTransportConfigContainer {

    boolean shouldGenerateBusFailures();

    void setShouldGenerateBusFailures(boolean generateBusFailures);

    boolean wasBusCrashGeneratedOnce();

    void setBusCrashGeneratedOnce(boolean busCrashGeneratedOnce);

    boolean isTransportChangeStrategyActive();

    void setTransportChangeStrategyActive(boolean transportChangeStrategyActive);
}
