package smartcity.stations;

import org.javatuples.Pair;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class PedestrianArrivalInfo {
    List<Pair<String, Instant>> agentNamesAndArrivalTimes = new ArrayList<Pair<String, Instant>>();

    void putPedestrianOnList(Pair<String, Instant> pedestrian) {
        agentNamesAndArrivalTimes.add(pedestrian);
    }
}
