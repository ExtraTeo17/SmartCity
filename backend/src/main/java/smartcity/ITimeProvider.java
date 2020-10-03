package smartcity;

import java.time.LocalDateTime;

// TODO: Date will be probably changed to Instant/long
public interface ITimeProvider {
    LocalDateTime getCurrentSimulationTime();

    void setSimulationStartTime(LocalDateTime simulationTime);

    LocalDateTime getStartSimulationTime();

    long getNanoStartTime();
}
