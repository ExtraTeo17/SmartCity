package smartcity.config.abstractions;

/**
 * Contains all station related configuration properties.
 */
public interface IStationConfigContainer {
    boolean isStationStrategyActive();

    void setStationStrategyActive(boolean stationStrategyActive);

    /**
     * @return extend light time in seconds
     */
    int getExtendWaitTime();

    void setExtendWaitTime(int extendWaitTime);

}
