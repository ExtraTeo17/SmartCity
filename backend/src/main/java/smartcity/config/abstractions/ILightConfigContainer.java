package smartcity.config.abstractions;

public interface ILightConfigContainer {
    boolean isLightStrategyActive();

    void setLightStrategyActive(boolean lightStrategyActive);

    /**
     * @return extend light time in seconds
     */
    int getExtendLightTime();

    void setExtendLightTime(int lightExtendTime);
}
