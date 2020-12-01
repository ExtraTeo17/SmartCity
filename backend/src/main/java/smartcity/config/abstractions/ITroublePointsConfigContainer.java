package smartcity.config.abstractions;

public interface ITroublePointsConfigContainer {

    boolean isConstructionSiteStrategyActive();

    void setConstructionSiteStrategyActive(boolean constructionSiteStrategyActive);
    
    void setBusCrashGeneratedOnce(boolean busCrashGeneratedOnce);
    
    boolean getBusCrashGeneratedOnce();

    boolean shouldGenerateConstructionSites();
    
    boolean shouldDetectTrafficJams();
    
    boolean shouldGenerateBusFailures();

    void setShouldGenerateConstructionSites(boolean constructionSiteGenerationActive);

    /**
     * @return time before accident in seconds
     */
    int getTimeBeforeTrouble();

    /**
     * @param timeBeforeTrouble time before accident in seconds
     */
    void setTimeBeforeTrouble(int timeBeforeTrouble);
    
    void setShouldDetectTrafficJam(boolean shouldDetectTrafficJam);

    boolean isTrafficJamStrategyActive();
    
    boolean isTransportChangeStrategyActive();
    
    void setTransportChangeStrategyActive(boolean transportChangeStrategyActive);

    void setTrafficJamStrategyActive(boolean changeRouteOnTrafficJam);

    boolean shouldUseFixedConstructionSites();

    void setUseFixedConstructionSites(boolean value);
}
