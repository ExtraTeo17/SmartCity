package smartcity.config.abstractions;

public interface ILightConfigContainer {
    boolean tryLockLightManagers();

    void unlockLightManagers();

    boolean isLightStrategyActive();

    void setLightStrategyActive(boolean lightStrategyActive);

    /**
     * @return extend light time in seconds
     */
    int getExtendLightTime();
}
