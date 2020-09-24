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

    void addEntryToTimetable(long stationOsmId, String time) {
        Date timeOnStation;
        try {
            timeOnStation = new SimpleDateFormat("HH:mm:ss").parse(time);
        } catch (ParseException e) {
            logger.warn("Error parsing new entry", e);
            return;
        }

        if (timeOnStationChronological.size() == 0 ||
                timeOnStation.after(timeOnStationChronological.get(timeOnStationChronological.size() - 1))) {
            stationOsmIdToTime.put(stationOsmId, timeOnStation);
            timeOnStationChronological.add(timeOnStation);
        }
    }
}
