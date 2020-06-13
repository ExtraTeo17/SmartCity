package SmartCity.Buses;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import Routing.RouteNode;
import SmartCity.SmartCityAgent;
import jade.wrapper.AgentContainer;

public class Timetable {
	
	private Map<Long, Date> stationOsmIdToTime = new HashMap<>();
	private List<Pair<Date, Long>> timeOnStationChronological = new ArrayList<>();
	
	private Date getTimeOnStation(final long stationOsmId) {
		return stationOsmIdToTime.get(stationOsmId);
	}
	
	public void addEntryToTimetable(long stationOsmId, String time) {
		Date timeOnStation = null;
		try {
			timeOnStation = new SimpleDateFormat("HH:mm:ss").parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (timeOnStationChronological.size() == 0 ||
				(timeOnStation != null && timeOnStation.after(timeOnStationChronological
				.get(timeOnStationChronological.size() - 1).getValue0()))) {
			stationOsmIdToTime.put(stationOsmId, timeOnStation);
			timeOnStationChronological.add(Pair.with(timeOnStation, stationOsmId));
		}
	}

	public void createAgent(AgentContainer container, List<RouteNode> route, final String busLine,
			final String brigadeNr) {
		SmartCityAgent.tryAddNewBusAgent(this, route, busLine, brigadeNr);
	}
}
