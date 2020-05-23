package SmartCity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// One Bus
public class BrigadeInfo {
	private List<Timetable> timetable = new ArrayList<>();
	private int nr_brigada_id;
	private int counter_of_timetables=0;
	public BrigadeInfo(){
		counter_of_timetables=-1;
		stampCounter();
	}
	private void stampCounter() {
		counter_of_timetables++;
		timetable.add(new Timetable());
		
	}
	public void addToTimeTable(long stationOsmId, Date date) {
		if(timetable.get(counter_of_timetables).wasAltered()) {
			stampCounter();
		}
		timetable.get(counter_of_timetables).addEntryToTimetable(stationOsmId, date);
	}
	
}
