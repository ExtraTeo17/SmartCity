package smartcity;

import java.util.Date;

// TODO: Date will be probably changed to Instant/long
public interface ITimeProvider {
    Date getCurrentSimulationTime();

    Date getCurrentRealTime();

    void setSimulationStartTime(Date simulationTime);

    Date getStartSimulationTime();

    Date getStartRealTime();
}
