package smartcity;

import java.util.Date;

// TODO: Date will be probably changed to Instant/long
public interface ITimeManager {
    Date getCurrentSimulationTime();

    Date getCurrentRealTime();

    void setSimulationStartTime(Date simulationTime);

    Date getStartSimulationTime();

    Date getStartRealTime();
}
