package smartcity.buses;

import org.jetbrains.annotations.NotNull;
import routing.RouteNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class BrigadeInfo implements Iterable<Timetable> {
    private final String brigadeNr;
    private final List<Timetable> timetables = new ArrayList<>();
    private int timetablesCounter;
    private long currentlyConsideredStation = -1;

    public BrigadeInfo(final String brigadeNr) {
        this.brigadeNr = brigadeNr;
    }

    public String getBrigadeNr() {
        return brigadeNr;
    }

    private void stampCounterAndUpdateTimetableList(final long stationOsmId) {
        stampCounter(stationOsmId);
        updateTimetableList();
    }

    private void stampCounter(final long stationOsmId) {
        if (shouldReset(stationOsmId)) {
            timetablesCounter = 0;
            currentlyConsideredStation = stationOsmId;
        }
        else {
            ++timetablesCounter;
        }
    }

    private boolean shouldReset(final long stationOsmId) {
        return stationOsmId != currentlyConsideredStation;
    }

    private void updateTimetableList() {
        if (timetables.size() == timetablesCounter) {
            timetables.add(new Timetable());
        }
    }

    public void addToTimetable(long stationOsmId, String time) {
        stampCounterAndUpdateTimetableList(stationOsmId);
        timetables.get(timetablesCounter).addEntryToTimetable(stationOsmId, time);
    }

    @NotNull
    @Override
    public Iterator<Timetable> iterator() {
        return timetables.iterator();
    }

    @Override
    public void forEach(Consumer<? super Timetable> action) {
        timetables.forEach(action);
    }

    @Override
    public Spliterator<Timetable> spliterator() {
        return timetables.spliterator();
    }
}
