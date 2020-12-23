package smartcity.stations;

import java.time.LocalDateTime;

/**
 * Information regarding the forecasted car arrival, mainly the anticipated time
 * when the car is going to arrive at an object.
 */
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
