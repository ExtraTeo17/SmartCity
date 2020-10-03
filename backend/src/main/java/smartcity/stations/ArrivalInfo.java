package smartcity.stations;

import java.time.LocalDateTime;

public class ArrivalInfo {
    public final String agentName;
    public final LocalDateTime arrivalTime;

    ArrivalInfo(String agentName, LocalDateTime arrivalTime) {
        this.agentName = agentName;
        this.arrivalTime = arrivalTime;
    }

    public static ArrivalInfo of(String agentName, LocalDateTime arrivalTime) {
        return new ArrivalInfo(agentName, arrivalTime);
    }
}
