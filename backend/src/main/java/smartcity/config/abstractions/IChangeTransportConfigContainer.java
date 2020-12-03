package smartcity.config.abstractions;

public interface IChangeTransportConfigContainer {

    boolean shouldGenerateBusFailures();

    void setGenerateBusFailures(boolean generateBusFailures);

    boolean wasBusCrashGeneratedOnce();

    void setBusCrashGeneratedOnce(boolean busCrashGeneratedOnce);

    boolean isTransportChangeStrategyActive();

    void setTransportChangeStrategyActive(boolean transportChangeStrategyActive);
}
