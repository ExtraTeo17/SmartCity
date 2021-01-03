package osmproxy.buses;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import osmproxy.OsmQueryManager;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.buses.abstractions.IBusApiManager;
import routing.core.IZone;
import utilities.ConditionalExecutor;
import utilities.FileWrapper;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class BusApiManager implements IBusApiManager {
    private static final Logger logger = LoggerFactory.getLogger(BusApiManager.class);

    private final IMapAccessManager mapAccessManager;

    @Inject
    public BusApiManager(IMapAccessManager mapAccessManager) {
        this.mapAccessManager = mapAccessManager;
    }

    @Override
    public Optional<Document> getBusDataXml(IZone zone) {
        var query = OsmQueryManager.getBusQuery(zone.getCenter(), zone.getRadius());
        var overpassInfo = mapAccessManager.getNodesDocument(query);

        ConditionalExecutor.trace(() -> {
            logger.info("Writing bus-data to: " + FileWrapper.DEFAULT_OUTPUT_PATH_XML);
            //noinspection OptionalGetWithoutIsPresent
            FileWrapper.write(overpassInfo.get());
        }, overpassInfo.isPresent());

        return overpassInfo;
    }

    @Override
    public Optional<String> getBusTimetablesViaWarszawskieAPI(String busStopId, String busStopNr, String busLine) {
        var query = getBusTimetablesWarszawskieQuery(busStopId, busStopNr, busLine);
        String jsonString;
        try {
            URL url = new URL(query);
            Scanner scanner = new Scanner(url.openStream());
            StringBuilder jsonBuilder = new StringBuilder();
            while (scanner.hasNext()) {
                jsonBuilder.append(scanner.nextLine());
            }
            jsonString = jsonBuilder.toString();
        } catch (Exception e) {
            logger.warn("Error trying to get 'Warszawskie busy'", e);
            return Optional.empty();
        }

        ConditionalExecutor.trace(() -> {
            String path = "target/line_" + busLine + "_stop_" + busStopId + "_" + busStopNr + ".json";
            logger.info("Writing bus-brigade-date to: " + path);
            FileWrapper.write(jsonString, path);
        });

        return Optional.of(jsonString);
    }

    @Override
    public Optional<Document> getBusWays(List<Long> waysIds) {
        var query = buildWaysQuery(waysIds);
        var resultOpt = mapAccessManager.getNodesDocument(query);

        ConditionalExecutor.trace(() -> {
            //noinspection OptionalGetWithoutIsPresent
            var result = resultOpt.get();
            String path = "target/busWays_" + waysIds.get(0) + "_" +
                    waysIds.get(waysIds.size() - 1) + ".xml";
            logger.info("Writing bus-ways to: " + path);
            FileWrapper.write(result, path);
        }, resultOpt.isPresent());

        return resultOpt;
    }

    private String buildWaysQuery(List<Long> waysIds) {
        StringBuilder busWayQueryBuilder = new StringBuilder();
        for (var id : waysIds) {
            busWayQueryBuilder.append(OsmQueryManager.getSingleBusWayQuery(id));
        }

        return OsmQueryManager.getQueryWithPayload(busWayQueryBuilder.toString());
    }


    /**
     * @param busStopId Represents id of group of busStops - example 'Marszałkowska'
     * @param busStopNr Represents number of stop in group of busStops - example 01 -> 'Marszałkowska 01'
     * @param busLine   Represents concrete busLine, example '523'
     * @return Query string for downloading timetable for specified line from WarszawskieAPI
     */
    private static String getBusTimetablesWarszawskieQuery(String busStopId, String busStopNr, String busLine) {
        var request = "https://api.um.warszawa.pl/api/action/dbtimetable_get/?id=e923fa0e-d96c-43f9-ae6e-60518c9f3238&busstopId" +
                "=" + busStopId + "&busstopNr=" + busStopNr + "&line=" + busLine +
                "&apikey=3320a2ff-9dc5-4492-83f1-255eaf6778d4";
        logger.info(request);
        return request;
    }
}
