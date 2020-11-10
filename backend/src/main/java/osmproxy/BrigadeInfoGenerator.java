package osmproxy;

import osmproxy.buses.BrigadeInfo;
import osmproxy.buses.models.TimetableRecord;
import osmproxy.elements.OSMStation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class BrigadeInfoGenerator {
    private final Random random = new Random();
    private final int GEN_COUNT = 10;
    private final LocalDateTime startTime;

    public BrigadeInfoGenerator() {
        this.startTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
    }

    public Collection<BrigadeInfo> generate(String busLine, List<OSMStation> osmStations) {
        var result = new LinkedHashMap<String, BrigadeInfo>();

        // Brigade -> list of timetables
        // Timetables -> one bus ride
        //  Brigade
        random.setSeed(busLine.hashCode());
        for (int i = 0; i < GEN_COUNT; ++i) {
            var startStation = osmStations.get(0);
            int brigadeIntervalMin = random.nextInt(10);
            var brigade = new BrigadeInfo("bg" + i, startStation.getId(), );
            for (int statIter = 1; statIter < osmStations.size(); ++statIter) {
                brigade.addTimetableRecords()
            }
        }


        return result.values();
    }

    private List<TimetableRecord> getRecords() {
        var result = new ArrayList<TimetableRecord>();


        return result;
    }
}
