package smartcity.buses;

import jade.wrapper.AgentContainer;
import org.javatuples.Pair;
import routing.RouteNode;
import smartcity.MasterAgent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Timetable {

    private Map<Long, Date> stationOsmIdToTime = new HashMap<>();
    private List<Pair<Date, Long>> timeOnStationChronological = new ArrayList<>();

    public Date getTimeOnStation(final long stationOsmId) {
        return stationOsmIdToTime.get(stationOsmId);
    }

    public Date getBoardingTime() {
        return timeOnStationChronological.get(0).getValue0();
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
        MasterAgent.tryAddNewBusAgent(this, route, busLine, brigadeNr);
    }
}
