package smartcity.stations;

import java.time.LocalDateTime;

public class ScheduledArrivalTime  {
    public final LocalDateTime actual;
    public final LocalDateTime scheduled;

    private ScheduledArrivalTime(LocalDateTime scheduled,
                                LocalDateTime actual) {
        this.scheduled = scheduled;
        this.actual = actual;
    }

    static ScheduledArrivalTime of(LocalDateTime scheduled, LocalDateTime actual){
        return new ScheduledArrivalTime(scheduled, actual);
    }
}
