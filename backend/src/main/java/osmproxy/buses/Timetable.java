package osmproxy.buses;

import com.google.common.collect.TreeMultiset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Timetable {
    private static final Logger logger = LoggerFactory.getLogger(Timetable.class);

    private final Map<Long, LocalDateTime> stopIdToTime;
    private final TreeMultiset<LocalDateTime> stopTimesChronological;

    Timetable(long startStopId, LocalDateTime startDate) {
        this.stopIdToTime = new HashMap<>();
        this.stopIdToTime.put(startStopId, startDate);

        this.stopTimesChronological = TreeMultiset.create();
        this.stopTimesChronological.add(startDate);
    }

    public LocalDateTime getBoardingTime() {
        return stopTimesChronological.firstEntry().getElement();
    }

    public Optional<LocalDateTime> getTimeOnStation(final long stationId) {
        var time = stopIdToTime.get(stationId);
        return Optional.ofNullable(time);
    }

    void addTimeRecord(long stationId, LocalDateTime time) {
        stopIdToTime.put(stationId, time);
        stopTimesChronological.add(time);
    }
}
