package osmproxy;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.elements.OSMStation;
import smartcity.MasterAgent;
import smartcity.buses.BrigadeInfo;
import smartcity.buses.BusInfo;

import java.net.URL;
import java.util.*;

public class BusLinesManager {
    private static final Logger logger = LoggerFactory.getLogger(BusLinesManager.class);
    private static final JSONParser jsonParser = new JSONParser();

    public static Set<BusInfo> getBusInfo(int radius, double middleLat, double middleLon) {
        logger.info("STEP 2/" + MasterAgent.STEPS + ": Sending bus overpass query");
        Set<BusInfo> infoSet = MapAccessManager.sendBusOverpassQuery(radius, middleLat, middleLon);
        logger.info("STEP 4/" + MasterAgent.STEPS + ": Starting warszawskie query and parsing");
        int i = 0;
        for (BusInfo info : infoSet) {
            logger.info("STEP 4/" + MasterAgent.STEPS + " (SUBSTEP " + (++i) + "/" + infoSet.size() + "): Warszawskie query sending & parsing substep");
            sendBusWarszawskieQuery(info);
        }
        return infoSet;
    }

    private static void sendBusWarszawskieQuery(BusInfo info) {
        Map<String, BrigadeInfo> brigadeNrToBrigadeInfo = new HashMap<>();
        var busLine = info.getBusLine();
        for (OSMStation station : info.getStations()) {
            var query = getBusWarszawskieQuery(station.getBusStopId(), station.getBusStopNr(), busLine);
            var nodesOptional = getNodesViaWarszawskie(query);
            nodesOptional.ifPresent(jsonObject -> parseBusInfo(brigadeNrToBrigadeInfo, station, jsonObject));
        }
        info.setBrigadeList(brigadeNrToBrigadeInfo.values());
    }

    private static Optional<JSONObject> getNodesViaWarszawskie(String query) {
        URL url;
        Scanner scanner;
        StringBuilder json = new StringBuilder();
        JSONObject jObject;
        try {
            url = new URL(query);
            scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                json.append(scanner.nextLine());
            }
            jObject = (JSONObject) jsonParser.parse(json.toString());
        } catch (Exception e) {
            logger.warn("Error trying to get 'Warszawskie busy'", e);
            return Optional.empty();
        }

        return Optional.of(jObject);
    }

    private static void parseBusInfo(Map<String, BrigadeInfo> brigadeNrToBrigadeInfo, OSMStation station, JSONObject jsonObject) {
        JSONArray msg = (JSONArray) jsonObject.get("result");
        String currentBrigadeNr = "";
        for (Object o : msg) {
            JSONObject values = (JSONObject) o;
            JSONArray valuesArray = (JSONArray) values.get("values");
            for (Object item : valuesArray) {
                JSONObject valueObject = (JSONObject) item;
                String key = (String) valueObject.get("key");
                String value = (String) valueObject.get("value");
                if (key.equals("brygada")) {
                    currentBrigadeNr = value;
                    if (!brigadeNrToBrigadeInfo.containsKey(value)) {
                        brigadeNrToBrigadeInfo.put(value, new BrigadeInfo(value));
                    }
                }
                else if (key.equals("czas")) {
                    brigadeNrToBrigadeInfo.get(currentBrigadeNr).addToTimetable(station.getId(), value);
                }
            }
        }
    }

    private static String getBusWarszawskieQuery(String busStopId, String busStopNr, String busLine) {
        return "https://api.um.warszawa.pl/api/action/dbtimetable_get/?id=e923fa0e-d96c-43f9-ae6e-60518c9f3238&busstopId=" + busStopId + "&busstopNr=" + busStopNr + "&line=" + busLine + "&apikey=400dacf8-9cc4-4d6c-82cc-88d9311401a5";
    }
}
