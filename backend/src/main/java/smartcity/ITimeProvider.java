package smartcity;

import java.time.LocalDateTime;

// TODO: Date will be probably changed to Instant/long
public interface ITimeProvider {
    LocalDateTime getCurrentSimulationTime();

    void setSimulationStartTime(LocalDateTime simulationTime);

    LocalDateTime getStartSimulationTime();

    long getTicks();

    Runnable getUpdateTimeTask(long initialTicks);

    default Runnable getUpdateTimeTask() {
        return getUpdateTimeTask(0);
    }

    default Runnable getUpdateTimeTask(LocalDateTime simulationStartTime) {
        setSimulationStartTime(simulationStartTime);
        return getUpdateTimeTask();
    }

    default Runnable getUpdateTimeTask(LocalDateTime simulationStartTime, long initialTicks) {
        setSimulationStartTime(simulationStartTime);
        return getUpdateTimeTask(initialTicks);
    }
}
