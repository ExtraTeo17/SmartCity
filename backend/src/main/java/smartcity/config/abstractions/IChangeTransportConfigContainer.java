package smartcity.config.abstractions;

/**
 * Controls configuration in the case of crash of the bus
 */
public interface IChangeTransportConfigContainer {

    boolean shouldGenerateBusFailures();

    void setShouldGenerateBusFailures(boolean generateBusFailures);

    boolean wasBusCrashGeneratedOnce();

    void setBusCrashGeneratedOnce(boolean busCrashGeneratedOnce);

    boolean isTransportChangeStrategyActive();

    void setTransportChangeStrategyActive(boolean transportChangeStrategyActive);
}
