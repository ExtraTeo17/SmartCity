package smartcity.buses;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Timetable {
    private static final Logger logger = LoggerFactory.getLogger(Timetable.class);
    private final Map<Long, Date> stationOsmIdToTime = new HashMap<>();
    private final List<Pair<Date, Long>> timeOnStationChronological = new ArrayList<>();

    public Optional<Date> getTimeOnStation(final long stationId) {
        var time = stationOsmIdToTime.get(stationId);
        if (time == null) {
            logger.warn("Could not retrieve time for " + stationId);
            return Optional.empty();
        }

        return Optional.of(time);
    }

    public Date getBoardingTime() {
        return timeOnStationChronological.get(0).getValue0();
    }

    void addEntryToTimetable(long stationOsmId, String time) {
        Date timeOnStation = null;
        try {
            timeOnStation = new SimpleDateFormat("HH:mm:ss").parse(time);
        } catch (ParseException e) {
            logger.warn("Error parsing new entry", e);
        }
        if (timeOnStationChronological.size() == 0 ||
                (timeOnStation != null && timeOnStation.after(timeOnStationChronological
                        .get(timeOnStationChronological.size() - 1).getValue0()))) {
            stationOsmIdToTime.put(stationOsmId, timeOnStation);
            timeOnStationChronological.add(Pair.with(timeOnStation, stationOsmId));
        }
    }
}
