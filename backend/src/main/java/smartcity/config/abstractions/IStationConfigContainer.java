package smartcity.config.abstractions;
//TODO:dokumentacja

public interface IStationConfigContainer {
    boolean isStationStrategyActive();

    void setStationStrategyActive(boolean stationStrategyActive);

    /**
     * @return extend light time in seconds
     */
    int getExtendWaitTime();

    void setExtendWaitTime(int extendWaitTime);

}
