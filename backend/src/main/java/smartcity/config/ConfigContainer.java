package smartcity.config;

import com.google.inject.Inject;
import routing.core.IGeoPosition;
import routing.core.IZone;
import routing.core.Position;
import routing.core.Zone;
import smartcity.SimulationState;


public class ConfigContainer extends ConfigMutator
        implements IZoneMutator, ILightConfigContainer,
        IConstructionSiteConfigContainer {
    private SimulationState simulationState = SimulationState.INITIAL;
    private boolean shouldGeneratePedestriansAndBuses = false;
    private boolean shouldGenerateCars = true;
    private boolean isLightStrategyActive = false;
    private boolean isConstructionSiteStrategyActive = false;
	private boolean isConstructionSiteGenerationActive = false;
    private boolean lightManagersLock = false;
    private int extendTimeSeconds = 30;

    private final IZone zone;
    private final ObjectsConfig carsConfig;

    @Inject
    public ConfigContainer() {
        IGeoPosition warsawPos = Position.of(52.23682, 21.01681);
        int defaultRadius = 600;
        this.zone = Zone.of(warsawPos, defaultRadius);

        int defaultTestCarId = 2;
        int defaultCarsNum = 4;
        this.carsConfig = CarsConfig.of(defaultCarsNum, defaultTestCarId);
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

    public int getTestCarId() {
        return carsConfig.getTestObjectNumber();
    }

    public void setTestCarId(int id) {
        carsConfig.setTestObjectNumber(mutation, id);
    }

    public int getCarsNumber() {
        return carsConfig.getNumber();
    }

    public void setCarsNumber(int num) {
        carsConfig.setNumber(mutation, num);
    }

    // TODO: Why the override is here, Przemek?
    @Override
    public boolean isLightStrategyActive() {
        return isLightStrategyActive;
    }

    @Override
    public void setLightStrategyActive(boolean lightStrategyActive) {
        isLightStrategyActive = lightStrategyActive;
    }
    
    @Override
    public boolean isConstructionSiteStrategyActive() {
    	return isConstructionSiteStrategyActive;
    }
    
    @Override
    public void setConstructionSiteStrategyActive(boolean constructionSiteStrategyActive) {
    	isConstructionSiteStrategyActive = constructionSiteStrategyActive;
    }

    public int getExtendTimeSeconds() {
        return extendTimeSeconds;
    }

	@Override
	public boolean isConstructionSiteGenerationActive() {
		return isConstructionSiteGenerationActive;
	}

	@Override
	public void setConstructionSiteGenerationActive(boolean constructionSiteGenerationActive) {
		isConstructionSiteGenerationActive = constructionSiteGenerationActive;
	}
}
