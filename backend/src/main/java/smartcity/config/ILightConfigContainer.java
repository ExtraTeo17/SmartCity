package smartcity.config;

public interface ILightConfigContainer {
    boolean tryLockLightManagers();

    void unlockLightManagers();

    boolean isLightStrategyActive();

    void setLightStrategyActive(boolean lightStrategyActive);
}
