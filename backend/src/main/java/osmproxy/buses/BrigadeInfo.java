package osmproxy.buses;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.buses.models.TimetableRecord;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class BrigadeInfo implements Iterable<Timetable>, Serializable {
    private final static Logger logger = LoggerFactory.getLogger(BrigadeInfo.class);

    public final String brigadeId;
    private final List<Timetable> timetables;

    BrigadeInfo(String brigadeId,
                long initialStopId,
                List<TimetableRecord> initialStopTimestamps) {
        this.brigadeId = brigadeId;
        this.timetables = initialStopTimestamps.stream()
                .map(record -> new Timetable(initialStopId, record.timeOnStop))
                .collect(Collectors.toList());
    }

    BrigadeInfo(String brigadeId,
                long initialStopId,
                List<Timetable> initialStopTimetables) {
        this.brigadeId = brigadeId;
        this.timetables = initialStopTimestamps.stream()
                .map(record -> new Timetable(initialStopId, record.timeOnStop))
                .collect(Collectors.toList());
    }

    public void addTimetableRecords(long stationId, List<TimetableRecord> timetableRecords) {
        if (timetables.size() != timetableRecords.size()) {
            logger.error("Initial timetables size different than current:\n" +
                    " initial: '" + timetables.size() +
                    " current: " + timetableRecords.size());
        }

        for (int i = 0; i < timetables.size() && i < timetableRecords.size(); ++i) {
            var timetable = timetables.get(i);
            var record = timetableRecords.get(i);
            timetable.addTimeRecord(stationId, record.timeOnStop);
        }
    }

    @NotNull
    @Override
    public Iterator<Timetable> iterator() {
        return timetables.iterator();
    }
}
