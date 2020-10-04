package smartcity.config;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import events.web.SimulationReadyEvent;
import routing.core.IGeoPosition;
import routing.core.IZone;
import routing.core.Position;
import routing.core.Zone;
import smartcity.SimulationState;


public class ConfigContainer extends ConfigMutator
        implements IZoneMutator, ILightConfigContainer {
    private final EventBus eventBus;
    private SimulationState simulationState = SimulationState.INITIAL;
    private boolean shouldGeneratePedestriansAndBuses = true;
    private boolean shouldGenerateCars = true;
    private boolean lightManagersLock = false;
    private boolean isLightStrategyActive = true;
    private final IZone zone;
    private final ObjectsConfig carsConfig;

    @Inject
    public ConfigContainer(EventBus eventBus) {
        this.eventBus = eventBus;

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
            if (simulationState == SimulationState.READY_TO_RUN) {
                eventBus.post(new SimulationReadyEvent());
            }
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

    @Override
    public boolean isLightStrategyActive() {
        return isLightStrategyActive;
    }

    @Override
    public void setLightStrategyActive(boolean lightStrategyActive) {
        isLightStrategyActive = lightStrategyActive;
    }
}
