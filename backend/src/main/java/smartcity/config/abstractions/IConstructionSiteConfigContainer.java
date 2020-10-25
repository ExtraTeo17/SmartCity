package smartcity.config.abstractions;

public interface IConstructionSiteConfigContainer {

    boolean isConstructionSiteStrategyActive();

    void setConstructionSiteStrategyActive(boolean constructionSiteStrategyActive);

    boolean shouldGenerateConstructionSites();

    void setShouldGenerateConstructionSites(boolean constructionSiteGenerationActive);
}
