package osmproxy.buses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Timetable {
    private static final Logger logger = LoggerFactory.getLogger(Timetable.class);
    private final Map<Long, Date> stationOsmIdToTime = new HashMap<>();
    private final List<Date> timeOnStationChronological = new LinkedList<>();

    Timetable() { }

    public Optional<Date> getTimeOnStation(final long stationId) {
        var time = stationOsmIdToTime.get(stationId);
        return Optional.ofNullable(time);
    }

    public Date getBoardingTime() {
        return timeOnStationChronological.get(0);
    }

    void addEntryToTimetable(long stationId, String time) {
        Date timeOnStation;
        try {
            timeOnStation = new SimpleDateFormat("HH:mm:ss").parse(time);
        } catch (ParseException e) {
            logger.warn("Error parsing new entry", e);
            return;
        }

        var lastEntry = getLastChronologicalEntry();
        if (lastEntry.isEmpty() || !timeOnStation.before(lastEntry.get())) {
            stationOsmIdToTime.put(stationId, timeOnStation);
            timeOnStationChronological.add(timeOnStation);
        }
        else {
            logger.debug("Did not put '" + time + "' for '" + stationId + "'\n"
                    + "lastEntry: '" + lastEntry.orElse(null) + "'");
        }
    }

    private Optional<Date> getLastChronologicalEntry() {
        return timeOnStationChronological.size() != 0 ?
                Optional.of(timeOnStationChronological.get(timeOnStationChronological.size() - 1)) :
                Optional.empty();
    }
}
