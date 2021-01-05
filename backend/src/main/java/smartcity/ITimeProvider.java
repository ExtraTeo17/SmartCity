package smartcity;

import java.time.LocalDateTime;
//TODO:dokumentacja

public interface ITimeProvider {
    int getTimeScale();

    void setTimeScale(int timeScale);

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

    default Runnable getUpdateTimeTask(int timeScale, LocalDateTime simulationStartTime) {
        setTimeScale(timeScale);
        return getUpdateTimeTask(simulationStartTime);
    }

    default Runnable getUpdateTimeTask(LocalDateTime simulationStartTime, long initialTicks) {
        setSimulationStartTime(simulationStartTime);
        return getUpdateTimeTask(initialTicks);
    }
}
