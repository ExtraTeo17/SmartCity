package smartcity.config.abstractions;
//TODO:dokumentacja

public interface IChangeTransportConfigContainer {

    boolean shouldGenerateBusFailures();

    void setShouldGenerateBusFailures(boolean generateBusFailures);

    boolean wasBusCrashGeneratedOnce();

    void setBusCrashGeneratedOnce(boolean busCrashGeneratedOnce);

    boolean isTransportChangeStrategyActive();

    void setTransportChangeStrategyActive(boolean transportChangeStrategyActive);
}
