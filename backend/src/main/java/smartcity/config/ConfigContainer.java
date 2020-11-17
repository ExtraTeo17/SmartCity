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
        ITroublePointsConfigContainer,
        IStationConfigContainer,
        IGenerationConfigContainer {

    private SimulationState simulationState = SimulationState.INITIAL;
    private boolean shouldGeneratePedestriansAndBuses = false;
    private boolean shouldGenerateConstructionSites = false;

    private boolean isLightStrategyActive = true;
    private boolean changeRouteOnTroublePoint = false;
    private boolean isStationStrategyActive = true;
    private boolean changeRouteOnTrafficJam = false;
    private boolean shouldUseFixedRoutes = false;
    private boolean shouldUseFixedConstructionSites = true;

    private int lightExtendTime = 30;
    private int extendWaitTime = 60;
    private int timeBeforeTrouble = 5000;

    private final IZone zone;

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

    public void setSimulationState(SimulationState simulationState) {
        if (this.simulationState != simulationState) {
            this.simulationState = simulationState;
        }
    }

    @Override
    public boolean isLightStrategyActive() {
        return isLightStrategyActive;
    }

    @Override
    public void setLightStrategyActive(boolean lightStrategyActive) {
        isLightStrategyActive = lightStrategyActive;
    }

    @Override
    public int getExtendWaitTime() {
        return extendWaitTime;
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
    public boolean shouldChangeRouteOnTroublePoint() {
        return changeRouteOnTroublePoint;
    }

    @Override
    public void setChangeRouteOnTroublePoint(boolean changeRouteOnTroublePoint) {
        this.changeRouteOnTroublePoint = changeRouteOnTroublePoint;
    }

    @Override
    public boolean shouldGenerateConstructionSites() {
        return shouldGenerateConstructionSites;
    }

    @Override
    public void setShouldGenerateConstructionSites(boolean constructionSiteGenerationActive) {
        shouldGenerateConstructionSites = constructionSiteGenerationActive;
    }

    @Override
    public boolean isStationStrategyActive() {
        return isStationStrategyActive;
    }

    @Override
    public void setStationStrategyActive(boolean stationStrategyActive) {
        isStationStrategyActive = stationStrategyActive;
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
    public boolean shouldChangeRouteOnTrafficJam() {
        return changeRouteOnTrafficJam;
    }

    @Override
    public void setChangeRouteOnTrafficJam(boolean changeRouteOnTrafficJam) {
        this.changeRouteOnTrafficJam = changeRouteOnTrafficJam;
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
    public void setUseFixedConstructionSites(boolean value) {
        shouldUseFixedConstructionSites = value;
    }

}
