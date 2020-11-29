package smartcity.config.abstractions;

public interface ITroublePointsConfigContainer {

    boolean shouldChangeRouteOnTroublePoint();

    void setChangeRouteOnTroublePoint(boolean constructionSiteStrategyActive);

    boolean shouldGenerateConstructionSites();
    
    boolean shouldGenerateCrashForBuses();

    void setShouldGenerateConstructionSites(boolean constructionSiteGenerationActive);

    /**
     * @return time before accident in seconds
     */
    int getTimeBeforeTrouble();

    /**
     * @param timeBeforeTrouble time before accident in seconds
     */
    void setTimeBeforeTrouble(int timeBeforeTrouble);

    boolean shouldChangeRouteOnTrafficJam();

    void setChangeRouteOnTrafficJam(boolean changeRouteOnTrafficJam);

    boolean shouldUseFixedConstructionSites();

    void setUseFixedConstructionSites(boolean value);
}
