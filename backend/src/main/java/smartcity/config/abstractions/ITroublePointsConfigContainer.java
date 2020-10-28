package smartcity.config.abstractions;

public interface ITroublePointsConfigContainer {

    boolean isChangeRouteStrategyActive();

    void setChangeRouteStrategyActive(boolean constructionSiteStrategyActive);

    boolean shouldGenerateConstructionSites();

    void setShouldGenerateConstructionSites(boolean constructionSiteGenerationActive);

    /**
     * @return time before accident in seconds
     */
    int getTimeBeforeTrouble();

    /**
     * @param timeBeforeTrouble time before accident in seconds
     */
    void setTimeBeforeTrouble(int timeBeforeTrouble);

    boolean shouldGenerateTrafficJams();

    void setShouldGenerateTrafficJams(boolean value);
}
