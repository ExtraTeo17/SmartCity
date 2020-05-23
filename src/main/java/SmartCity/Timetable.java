package SmartCity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jade.wrapper.AgentContainer;

public class Timetable {
	
	private Map<Long, Date> stationOsmIdToTime = new HashMap<>();
	
	private Date getTimeOnStation(final long stationOsmId) {
		return stationOsmIdToTime.get(stationOsmId);
	}
	
	public void addEntryToTimetable(long stationOsmId, String time) {
		try {
			stationOsmIdToTime.put(stationOsmId, new SimpleDateFormat("HH:mm:ss").parse(time));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void createAgent(AgentContainer container) {
		SmartCityAgent.tryAddNewBusAgent(this);
	}
}
