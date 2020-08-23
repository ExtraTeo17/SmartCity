package smartcity;

import java.util.Date;

public interface ITimeManager {
    Date getCurrentSimulationTime();

    Date getCurrentRealTime();

    void setSimulationStartTime(Date simulationTime);

    Date getStartSimulationTime();

    Date getStartRealTime();
}
