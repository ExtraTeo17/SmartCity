package SmartCity;

import java.util.ArrayList;
import java.util.List;

import jade.wrapper.AgentContainer;

public class BrigadeInfo {
	
	private List<Timetable> timetables = new ArrayList<>();
	private final int brigadeNr;
	private int timetablesCounter;
	private long currentlyConsideredStation = -1;
	
	public BrigadeInfo(final String brigadeNr) {
		this.brigadeNr = Integer.parseInt(brigadeNr);
	}
	
	private void stampCounterAndUpdateTimetableList(final long stationOsmId) {
		stampCounter(stationOsmId);
		updateTimetableList();
	}
	
	private void stampCounter(final long stationOsmId) {
		if (shouldReset(stationOsmId)) {
			timetablesCounter = 0;
			currentlyConsideredStation = stationOsmId;
		} else
			++timetablesCounter;
	}

	private boolean shouldReset(final long stationOsmId) {
		return stationOsmId != currentlyConsideredStation;
	}
	
	private void updateTimetableList() {
		if (timetables.size() == timetablesCounter)
			timetables.add(new Timetable());
	}
	
	public void addToTimetable(long stationOsmId, String time) {
		stampCounterAndUpdateTimetableList(stationOsmId);
		timetables.get(timetablesCounter).addEntryToTimetable(stationOsmId, time);
	}

	public void prepareAgents(AgentContainer container) {
		for (Timetable timetable : timetables) {
			timetable.createAgent(container);
		}
	}
}
