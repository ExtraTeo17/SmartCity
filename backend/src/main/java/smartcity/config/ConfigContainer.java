package smartcity.config;

import com.google.inject.Inject;
import routing.core.IGeoPosition;
import routing.core.IZone;
import routing.core.Position;
import routing.core.Zone;
import smartcity.SimulationState;
import smartcity.config.abstractions.*;


@SuppressWarnings("ClassWithTooManyFields")
public class ConfigContainer extends ConfigMutator
        implements IZoneMutator,
        ILightConfigContainer,
        IGenerationConfigContainer,
        IStationConfigContainer,
        ITroublePointsConfigContainer {

    private SimulationState simulationState = SimulationState.INITIAL;

    private boolean generateBatchesForCars = false;
    private boolean shouldGeneratePedestriansAndBuses = false;
    private boolean shouldGenerateConstructionSites = false;
    private boolean shouldDetectTrafficJams = false;
    private boolean shouldGenerateBusFailures = true;

    private boolean lightStrategyActive = false;
    private boolean stationStrategyActive = false;
    private boolean constructionSiteStrategyActive = false;
    private boolean trafficJamStrategyActive = false;
    private boolean transportChangeStrategyActive = false;

    private boolean shouldUseFixedRoutes = false;
    private boolean shouldUseFixedConstructionSites = true;

    private int lightExtendTime = 30;
    private int extendWaitTime = 60;
    private int timeBeforeTrouble = 100000;

    private final IZone zone;
    private boolean busCrashGeneratedOnce = false;

    @Inject
    public ConfigContainer() {
        IGeoPosition warsawPos = Position.of(52.23682, 21.01681);
        int defaultRadius = 600;
        this.zone = Zone.of(warsawPos, defaultRadius);
    }

    public boolean shouldGeneratePedestriansAndBuses() {
        return shouldGeneratePedestriansAndBuses;
    }

    public void setGeneratePedestriansAndBuses(boolean value) {
        this.shouldGeneratePedestriansAndBuses = value;
    }

    public IZone getZone() {
        return zone;
    }

    @Override
    public void setZone(IGeoPosition pos, int radius) {
        zone.set(mutation, pos, radius);
    }

    public SimulationState getSimulationState() {
        return simulationState;
    }

    @Override
    public void setBusCrashGeneratedOnce(boolean busCrashGeneratedOnce) {
        this.busCrashGeneratedOnce = busCrashGeneratedOnce;
    }

    @Override
    public boolean getBusCrashGeneratedOnce() {
        return busCrashGeneratedOnce;
    }

    public void setSimulationState(SimulationState simulationState) {
        if (this.simulationState != simulationState) {
            this.simulationState = simulationState;
        }
    }

    @Override
    public boolean isLightStrategyActive() {
        return lightStrategyActive;
    }

    @Override
    public void setLightStrategyActive(boolean lightStrategyActive) {
        this.lightStrategyActive = lightStrategyActive;
    }

    @Override
    public int getExtendWaitTime() {
        return extendWaitTime;
    }

    @Override
    public boolean getGenerateBatchesForCars() {
        return generateBatchesForCars;
    }

    @Override
    public void setExtendWaitTime(int extendWaitTime) {
        this.extendWaitTime = extendWaitTime;
    }

    @Override
    public int getExtendLightTime() {
        return lightExtendTime;
    }

    @Override
    public void setExtendLightTime(int lightExtendTime) {
        this.lightExtendTime = lightExtendTime;
    }

    @Override
    public boolean isConstructionSiteStrategyActive() {
        return constructionSiteStrategyActive;
    }

    @Override
    public void setConstructionSiteStrategyActive(boolean constructionSiteStrategyActive) {
        this.constructionSiteStrategyActive = constructionSiteStrategyActive;
    }

    @Override
    public boolean shouldGenerateConstructionSites() {
        return shouldGenerateConstructionSites;
    }

    @Override
    public boolean shouldGenerateBusFailures() { return shouldGenerateBusFailures; }

    @Override
    public void setShouldGenerateConstructionSites(boolean constructionSiteGenerationActive) {
        shouldGenerateConstructionSites = constructionSiteGenerationActive;
    }

    @Override
    public boolean isStationStrategyActive() {
        return stationStrategyActive;
    }

    @Override
    public void setStationStrategyActive(boolean stationStrategyActive) {
        this.stationStrategyActive = stationStrategyActive;
    }

    @Override
    public int getTimeBeforeTrouble() {
        return timeBeforeTrouble;
    }

    @Override
    public void setTimeBeforeTrouble(int timeBeforeTrouble) {
        this.timeBeforeTrouble = timeBeforeTrouble;
    }

    @Override
    public boolean isTrafficJamStrategyActive() {
        return trafficJamStrategyActive;
    }

    @Override
    public void setTrafficJamStrategyActive(boolean isTrafficJamStrategyActive) {
        this.trafficJamStrategyActive = isTrafficJamStrategyActive;
    }

    @Override
    public boolean shouldUseFixedRoutes() {
        return shouldUseFixedRoutes;
    }

    @Override
    public void setUseFixedRoutes(boolean value) {
        shouldUseFixedRoutes = value;
    }

    @Override
    public boolean shouldUseFixedConstructionSites() {
        return shouldUseFixedConstructionSites;
    }

    @Override
    public void setUseFixedConstructionSites(boolean useFixedConstructionSites) {
        shouldUseFixedConstructionSites = useFixedConstructionSites;
    }

	@Override
	public boolean shouldDetectTrafficJams() {
		return shouldDetectTrafficJams;
	}

	@Override
	public void setShouldDetectTrafficJam(boolean shouldDetectTrafficJam) {
		this.shouldDetectTrafficJams = shouldDetectTrafficJam;
	}

	@Override
	public boolean isTransportChangeStrategyActive() {
		return transportChangeStrategyActive;
	}

	@Override
	public void setTransportChangeStrategyActive(boolean transportChangeStrategyActive) {
		this.transportChangeStrategyActive = transportChangeStrategyActive;
	}

}
