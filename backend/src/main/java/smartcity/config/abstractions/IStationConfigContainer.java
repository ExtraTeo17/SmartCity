package smartcity.config.abstractions;

/**
 * Configure station
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
