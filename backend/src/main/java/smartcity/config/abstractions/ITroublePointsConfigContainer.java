package smartcity.config.abstractions;

public interface ITroublePointsConfigContainer {

    boolean isChangeRouteStrategyActive();

    void setChangeRouteStrategyActive(boolean constructionSiteStrategyActive);

    boolean shouldGenerateConstructionSites();

    void setShouldGenerateConstructionSites(boolean constructionSiteGenerationActive);
}
