package osmproxy.buses;

import com.google.inject.Inject;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.buses.abstractions.IBusApiManager;
import osmproxy.buses.abstractions.IBusDataParser;
import osmproxy.buses.abstractions.IBusLinesManager;
import osmproxy.buses.data.BusPreparationData;
import osmproxy.elements.OSMStation;
import routing.core.IZone;
import utilities.ConditionalExecutor;
import utilities.IterableJsonArray;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class BusLinesManager implements IBusLinesManager {
    private static final Logger logger = LoggerFactory.getLogger(BusLinesManager.class);

    private final IBusApiManager busApiManager;
    private final IBusDataParser busDataParser;
    private final IZone zone;

    @Inject
    BusLinesManager(IBusApiManager busApiManager,
                    IBusDataParser busDataParser,
                    IZone zone) {
        this.busApiManager = busApiManager;
        this.busDataParser = busDataParser;
        this.zone = zone;
    }

    @Override
    public BusPreparationData getBusData() {
        var overpassInfo = busApiManager.getBusDataXml(zone);
        if (overpassInfo.isEmpty()) {
            return new BusPreparationData();
        }

        var busInfoData = busDataParser.parseBusData(overpassInfo.get());

        for (var busInfo : busInfoData.busInfos) {
            var brigadeInfos = generateBrigadeInfos(busInfo.busLine, busInfo.stops);
            busInfo.addBrigades(brigadeInfos);
        }

        return busInfoData;
    }


    // TODO: Move to busDataParser
    private Collection<BrigadeInfo> generateBrigadeInfos(String busLine, Collection<OSMStation> osmStations) {
        Map<String, BrigadeInfo> brigadeInfoMap = new LinkedHashMap<>();
        for (OSMStation station : osmStations) {
            var jsonObjOpt = busApiManager.getNodesViaWarszawskieAPI(station.getBusStopId(),
                    station.getBusStopNr(), busLine);
            if (jsonObjOpt.isEmpty()) {
                continue;
            }

            var jsonObject = jsonObjOpt.get();
            var stationId = station.getId();
            for (JSONObject valuesArray : IterableJsonArray.of(jsonObject, "result")) {
                var brigadeAndTime = IterableJsonArray.of(valuesArray, "values").stream()
                        .filter(pair -> {
                            var key = (String) pair.get("key");
                            return key.equals("brygada") || key.equals("czas");
                        })
                        .sorted(Comparator.comparing(p -> ((String) p.get("key"))))
                        .map(pair -> (String) pair.get("value"))
                        .toArray(String[]::new);

                var brigadeNr = brigadeAndTime[0];
                var brigadeInfo = brigadeInfoMap.get(brigadeNr);
                if (brigadeInfo == null) {
                    brigadeInfo = new BrigadeInfo(brigadeNr);
                    brigadeInfoMap.put(brigadeNr, brigadeInfo);
                }

                var time = brigadeAndTime[1];
                ConditionalExecutor.trace(() -> {
                        logBrigadeData(time, station, busLine);
                });
                brigadeInfo.addToTimetable(stationId, time);
            }
        }

        return brigadeInfoMap.values();
    }

    private void logBrigadeData(String time, OSMStation station, String busLine) {
        logger.info("Printing data for brigade1:\n" +
                "  time: " + time + "\n" +
                "  stationId: " + station.getId() + "\n" +
                "  busStopId: " + station.getBusStopId() + "\n" +
                "  busStopNr: " + station.getBusStopNr() + "\n" +
                "  busLine: " + busLine + "\n");
    }
}
