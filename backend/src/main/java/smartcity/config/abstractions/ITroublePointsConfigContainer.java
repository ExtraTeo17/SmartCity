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


    boolean shouldUseFixedConstructionSites();

    void setUseFixedConstructionSites(boolean value);
}
