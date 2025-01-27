package smartcity.config.abstractions;

/**
 * Contains all light related configuration properties.
 */
public interface ILightConfigContainer {
    boolean isLightStrategyActive();

    void setLightStrategyActive(boolean lightStrategyActive);

    /**
     * @return extend light time in seconds
     */
    int getExtendLightTime();

    void setExtendLightTime(int lightExtendTime);

    boolean shouldDetectTrafficJams();

    void setShouldDetectTrafficJam(boolean shouldDetectTrafficJam);

    boolean isTrafficJamStrategyActive();

    void setTrafficJamStrategyActive(boolean changeRouteOnTrafficJam);
}
