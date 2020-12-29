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
        IGenerationConfigContainer,
        ILightConfigContainer,
        IStationConfigContainer,
        ITroublePointsConfigContainer,
        IChangeTransportConfigContainer {

    private SimulationState simulationState = SimulationState.INITIAL;

    private boolean shouldGeneratePedestriansAndBuses = false;
    private boolean shouldGenerateConstructionSites = true;
    private boolean shouldDetectTrafficJams = false;
    private boolean shouldGenerateBusFailures = false;
    private boolean shouldGenerateBatchesForCars = false;

    private boolean lightStrategyActive = false;
    private boolean stationStrategyActive = false;
    private boolean constructionSiteStrategyActive = true;
    private boolean trafficJamStrategyActive = false;
    private boolean transportChangeStrategyActive = false;

    private boolean shouldUseFixedRoutes = false;
    private boolean shouldUseFixedConstructionSites = true;

    private int lightExtendTime = 30;
    private int extendWaitTime = 60;
    private int timeBeforeTrouble = 5;
    private int thresholdUntilIndexChange = 50;
    private int noConstructionSiteStrategyIndexFactor = 30;

    private final IZone zone;
    private boolean busCrashGeneratedOnce = false;

    @Inject
    public ConfigContainer() {
        IGeoPosition warsawPos = Position.of(52.23682, 21.01681);
        int defaultRadius = 600;
        this.zone = Zone.of(warsawPos, defaultRadius);
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

    public void setSimulationState(SimulationState simulationState) {
        if (this.simulationState != simulationState) {
            this.simulationState = simulationState;
        }
    }

    @Override
    public boolean shouldGeneratePedestriansAndBuses() {
        return shouldGeneratePedestriansAndBuses;
    }

    @Override
    public void setGeneratePedestriansAndBuses(boolean value) {
        this.shouldGeneratePedestriansAndBuses = value;
    }

    @Override
    public void setBusCrashGeneratedOnce(boolean busCrashGeneratedOnce) {
        this.busCrashGeneratedOnce = busCrashGeneratedOnce;
    }

    @Override
    public boolean wasBusCrashGeneratedOnce() {
        return busCrashGeneratedOnce;
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
    public boolean shouldGenerateBatchesForCars() {
        return shouldGenerateBatchesForCars;
    }

    @Override
    public void setShouldGenerateBatchesForCars(boolean generateBatchesForCars) {
        this.shouldGenerateBatchesForCars = generateBatchesForCars;
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
    public void setShouldGenerateBusFailures(boolean generateBusFailures) {
        shouldGenerateBusFailures = generateBusFailures;
    }

    @Override
    public void setShouldGenerateConstructionSites(boolean shouldGenerateConstructionSites) {
        this.shouldGenerateConstructionSites = shouldGenerateConstructionSites;
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

    @Override
    public int getThresholdUntilIndexChange() {
        return thresholdUntilIndexChange;
    }

    @Override
    public void setThresholdUntilIndexChange(int thresholdUntilIndexChange) {
        this.thresholdUntilIndexChange = thresholdUntilIndexChange;
    }

    @Override
    public int getNoConstructionSiteStrategyIndexFactor() {
        return noConstructionSiteStrategyIndexFactor;
    }

    @Override
    public void setNoConstructionSiteStrategyIndexFactor(int noConstructionSiteStrategyIndexFactor) {
        this.noConstructionSiteStrategyIndexFactor = noConstructionSiteStrategyIndexFactor;
    }
}
