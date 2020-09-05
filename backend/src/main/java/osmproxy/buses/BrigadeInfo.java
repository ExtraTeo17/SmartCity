package osmproxy.buses;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BrigadeInfo implements Iterable<Timetable> {
    private final String brigadeNr;
    private final List<Timetable> timetables = new ArrayList<>();
    private int timetablesCounter;
    private long currentlyConsideredStation = -1;

    BrigadeInfo(final String brigadeNr) {
        this.brigadeNr = brigadeNr;
    }

    public String getBrigadeNr() {
        return brigadeNr;
    }

    private boolean shouldReset(final long stationOsmId) {
        return stationOsmId != currentlyConsideredStation;
    }

    // TODO: Is shouldReset executed only at first or may be more than one time?
    public void addToTimetable(long stationOsmId, String time) {
        if (shouldReset(stationOsmId)) {
            timetablesCounter = 0;
            currentlyConsideredStation = stationOsmId;
        }
        else {
            ++timetablesCounter;
        }
        if (timetables.size() == timetablesCounter) {
            timetables.add(new Timetable());
        }
        timetables.get(timetablesCounter).addEntryToTimetable(stationOsmId, time);
    }

    @NotNull
    @Override
    public Iterator<Timetable> iterator() {
        return timetables.iterator();
    }
}
