package osmproxy.buses;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import osmproxy.MapAccessManager;
import osmproxy.OsmQueryManager;
import osmproxy.buses.abstractions.IBusApiManager;
import routing.core.IZone;
import utilities.ConditionalExecutor;
import utilities.FileWriterWrapper;

import java.net.URL;
import java.util.Optional;
import java.util.Scanner;

public class BusApiManager implements IBusApiManager {
    private static final Logger logger = LoggerFactory.getLogger(BusApiManager.class);
    private static final JSONParser jsonParser = new JSONParser();

    @Override
    public Optional<Document> getBusDataXml(IZone zone) {
        var overpassQuery = OsmQueryManager.getBusQuery(zone.getCenter(), zone.getRadius());
        Document overpassInfo;
        try {
            overpassInfo = MapAccessManager.getNodesViaOverpass(overpassQuery);
        } catch (Exception e) {
            logger.warn("Error getting bus info.", e);
            return Optional.empty();
        }

        ConditionalExecutor.debug(() -> {
            logger.info("Writing bus-data to: " + FileWriterWrapper.DEFAULT_OUTPUT_PATH_XML);
            FileWriterWrapper.write(overpassInfo);
        });


        return Optional.of(overpassInfo);
    }

    @Override
    public Optional<JSONObject> getNodesViaWarszawskieAPI(String busStopId, String busStopNr, String busLine) {
        var query = getBusWarszawskieQuery(busStopId, busStopNr, busLine);
        JSONObject jObject;
        try {
            URL url = new URL(query);
            Scanner scanner = new Scanner(url.openStream());
            StringBuilder jsonBuilder = new StringBuilder();
            while (scanner.hasNext()) {
                jsonBuilder.append(scanner.nextLine());
            }
            jObject = (JSONObject) jsonParser.parse(jsonBuilder.toString());
        } catch (Exception e) {
            logger.warn("Error trying to get 'Warszawskie busy'", e);
            return Optional.empty();
        }

        ConditionalExecutor.debug(() -> {
            String path = "target/brigade" + busStopId + ".json";
            logger.info("Writing bus-brigade-date to: " + path);
            FileWriterWrapper.write(jObject, path);
        });

        return Optional.of(jObject);
    }

    private static String getBusWarszawskieQuery(String busStopId, String busStopNr, String busLine) {
        return "https://api.um.warszawa.pl/api/action/dbtimetable_get/?id=e923fa0e-d96c-43f9-ae6e-60518c9f3238&busstopId=" +
                busStopId + "&busstopNr=" + busStopNr + "&line=" + busLine + "&apikey=400dacf8-9cc4-4d6c-82cc-88d9311401a5";
    }
}
