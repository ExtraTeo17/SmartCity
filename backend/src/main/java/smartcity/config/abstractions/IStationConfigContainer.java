package smartcity.config.abstractions;

public interface IStationConfigContainer {
    boolean isStationStrategyActive();

    void setStationStrategyActive(boolean stationStrategyActive);

    /**
     * @return extend light time in seconds
     */
    int getExtendWaitTime();

    void setExtendWaitTime(int extendWaitTime);

}
