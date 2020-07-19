package smartcity.buses;

import routing.RouteNode;
import jade.wrapper.AgentContainer;

import java.util.ArrayList;
import java.util.List;

public class BrigadeInfo {

    private final String brigadeNr;
    private List<Timetable> timetables = new ArrayList<>();
    private int timetablesCounter;
    private long currentlyConsideredStation = -1;

    public BrigadeInfo(final String brigadeNr) {
        this.brigadeNr = brigadeNr;
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

    public void prepareAgents(AgentContainer container, List<RouteNode> route, final String busLine) {
        for (Timetable timetable : timetables) {
            timetable.createAgent(container, route, busLine, brigadeNr);
        }
    }
}
