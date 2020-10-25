package smartcity.config;

public interface IConstructionSiteConfigContainer {
	
    boolean isConstructionSiteStrategyActive();

    void setConstructionSiteStrategyActive(boolean constructionSiteStrategyActive);
    
    boolean isConstructionSiteGenerationActive();
    
    void setConstructionSiteGenerationActive(boolean constructionSiteGenerationActive);
}
