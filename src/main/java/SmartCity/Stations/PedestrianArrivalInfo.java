package SmartCity.Stations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

public class PedestrianArrivalInfo {
	 List<Pair<String,Instant>> agentNamesAndArrivalTimes = new ArrayList<Pair<String, Instant>>();

	 public void putPedestrianOnList(Pair<String,Instant> pedestrian) {
		 agentNamesAndArrivalTimes.add(pedestrian);
	 }
}
