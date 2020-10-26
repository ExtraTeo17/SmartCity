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
        IStationConfigContainer ,
        IPedestriansConfigContainer {
    private SimulationState simulationState = SimulationState.INITIAL;
    private boolean shouldGeneratePedestriansAndBuses = false;
    private boolean shouldGenerateCars = true;
    private boolean lightManagersLock = false;
    private boolean isLightStrategyActive = true;
    private boolean isConstructionSiteStrategyActive = false;
    private boolean isConstructionSiteGenerationActive = false;
    private boolean isStationStrategyActive = true;
    private int lightExtendTime = 30;
    private int extendWaitTime = 60;

    private final IZone zone;
    private final ObjectsConfig pedestriansConfig;

    @Inject
    public ConfigContainer() {
        IGeoPosition warsawPos = Position.of(52.23682, 21.01681);
        int defaultRadius = 600;
        this.zone = Zone.of(warsawPos, defaultRadius);

        this.pedestriansConfig = PedestriansConfig.of(20, 8);
    }

    public boolean shouldGenerateCars() {
        return shouldGenerateCars;
    }

    public void setGenerateCars(boolean value) {
        this.shouldGenerateCars = value;
    }

    public boolean shouldGeneratePedestriansAndBuses() {
        return shouldGeneratePedestriansAndBuses;
    }

    public void setGeneratePedestriansAndBuses(boolean value) {
        this.shouldGeneratePedestriansAndBuses = value;
    }

    @Override
    public synchronized boolean tryLockLightManagers() {
        if (lightManagersLock) {
            return false;
        }

        lightManagersLock = true;
        return true;
    }

    @Override
    public synchronized void unlockLightManagers() {
        lightManagersLock = false;
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
        this.extendWaitTime =extendWaitTime;
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
    public boolean isChangeRouteStrategyActive() {
        return isConstructionSiteStrategyActive;
    }

    @Override
    public void setChangeRouteStrategyActive(boolean constructionSiteStrategyActive) {
        isConstructionSiteStrategyActive = constructionSiteStrategyActive;
    }

    @Override
    public boolean shouldGenerateConstructionSites() {
        return isConstructionSiteGenerationActive;
    }

    @Override
    public void setShouldGenerateConstructionSites(boolean constructionSiteGenerationActive) {
        isConstructionSiteGenerationActive = constructionSiteGenerationActive;
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
    public int getTestPedestrianId() {
        return pedestriansConfig.getTestObjectNumber();
    }

    @Override
    public void setTestPedestrianId(int id) {
        pedestriansConfig.setTestObjectNumber(mutation, id);
    }

    @Override
    public int getPedestriansNumber() {
        return pedestriansConfig.getNumber();
    }

    @Override
    public void setPedestriansNumber(int num) {
        pedestriansConfig.setNumber(mutation, num);
    }
}
