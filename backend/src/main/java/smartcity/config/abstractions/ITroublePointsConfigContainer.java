package smartcity.config.abstractions;

public interface ITroublePointsConfigContainer {

    boolean shouldGenerateConstructionSites();

    void setShouldGenerateConstructionSites(boolean shouldGenerateConstructionSites);

    boolean isConstructionSiteStrategyActive();

    void setConstructionSiteStrategyActive(boolean constructionSiteStrategyActive);

    /**
     * @return time before accident in seconds
     */
    int getTimeBeforeTrouble();

    /**
     * @param timeBeforeTrouble time before accident in seconds
     */
    void setTimeBeforeTrouble(int timeBeforeTrouble);

    int getThresholdUntilIndexChange();

    void setThresholdUntilIndexChange(int thresholdUntilIndexChange);

    int getNoConstructionSiteStrategyIndexFactor();

    /**
     * Lower values provide risk of car ignoring the trouble point and passing through it
     */
    void setNoConstructionSiteStrategyIndexFactor(int noConstructionSiteStrategyIndexFactor);

    boolean shouldUseFixedConstructionSites();

    void setUseFixedConstructionSites(boolean value);
}
