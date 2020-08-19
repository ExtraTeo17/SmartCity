package osmproxy.buses;

import com.google.inject.Inject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import osmproxy.MapAccessManager;
import osmproxy.OsmQueryManager;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;
import routing.IZone;
import routing.Position;
import smartcity.MasterAgent;
import smartcity.buses.BrigadeInfo;
import utilities.IterableNodeList;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class BusLinesManager implements IBusLinesManager {
    private static final Logger logger = LoggerFactory.getLogger(BusLinesManager.class);
    private static final JSONParser jsonParser = new JSONParser();
    private static final int BUS_PREPARATION_STEPS = 5;
    private final IZone zone;

    @Inject
    BusLinesManager(IZone zone) {
        this.zone = zone;
    }

    @Override
    public Set<BusInfo> getBusInfos() {
        logger.info("STEP 1/" + BUS_PREPARATION_STEPS + ": Starting bus preparation");
        logger.info("STEP 2/" + BUS_PREPARATION_STEPS + ": Sending bus overpass query");
        var overpassInfo = sendBusOverpassQuery();
        if (overpassInfo.isEmpty()) {
            return new HashSet<>();
        }

        logger.info("STEP 3/" + BUS_PREPARATION_STEPS + ": Starting overpass bus info parsing");
        Set<BusInfo> busInfoSet = parseBusInfos(overpassInfo.get());

        logger.info("STEP 4/" + BUS_PREPARATION_STEPS + ": Starting warszawskie query and parsing");
        logger.info("STEP 5/" + BUS_PREPARATION_STEPS + ": Starting agent preparation based on queries");
        int substepCounter = 0;
        for (var busInfo : busInfoSet) {
            logger.info("STEP 4/" + BUS_PREPARATION_STEPS + " (SUBSTEP " + (++substepCounter) + "/" + busInfoSet.size() +
                    "): Warszawskie query sending & parsing substep");
            var brigadeInfos = generateBrigadeInfos(busInfo);
            busInfo.setBrigadeList(brigadeInfos);
            logger.info("STEP 5/" + BUS_PREPARATION_STEPS + " (SUBSTEP " + (substepCounter) + "/" + busInfoSet.size() +
                    "): Agent preparation substep");
        }

        return busInfoSet;
    }

    private Optional<Document> sendBusOverpassQuery() {
        var overpassQuery = OsmQueryManager.getBusOverpassQuery(zone.getCenter(), zone.getRadius());
        Document overpassInfo;
        try {
            overpassInfo = MapAccessManager.getNodesViaOverpass(overpassQuery);
        } catch (Exception e) {
            logger.warn("Error getting bus info.", e);
            return Optional.empty();
        }

        return Optional.of(overpassInfo);
    }

    private Set<BusInfo> parseBusInfos(Document nodesViaOverpass) {
        Node osmRoot = nodesViaOverpass.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        Set<BusInfo> busInfos = new LinkedHashSet<>();

        int errors = 0;
        // TODO: Starts from 1, intended?
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            Node osmNode = osmXMLNodes.item(i);
            var nodeName = osmNode.getNodeName();
            if (nodeName.equals("relation")) {
                var busInfo = parseSingleBusInfo(osmNode);
                if (busInfo.isEmpty()) {
                    if (++errors < 5) {
                        continue;
                    }
                    throw new RuntimeException("Too much errors when parsing busInfo");
                }
                busInfos.add(busInfo.get());
            }
            else if (nodeName.equals("node")) {
                NamedNodeMap attributes = osmNode.getAttributes();
                long osmId = Long.parseLong(attributes.getNamedItem("id").getNodeValue());
                double lat = Double.parseDouble(attributes.getNamedItem("lat").getNodeValue());
                double lon = Double.parseDouble(attributes.getNamedItem("lon").getNodeValue());

                if (zone.contains(Position.of(lat, lon)) &&
                        !MasterAgent.osmIdToStationOSMNode.containsKey(osmId)) {
                    // TODO: Search for bus_stop
                    for (Node tag : IterableNodeList.of(osmNode.getChildNodes()).stream()
                            .filter(n -> n.getNodeName().equals("tag")).collect(Collectors.toList())) {
                        NamedNodeMap attr = tag.getAttributes();
                        Node key = attr.getNamedItem("k");
                        if (key.getNodeValue().equals("public_transport")) {
                            var value = attr.getNamedItem("v").getNodeValue();
                            if (!value.contains("stop")) {
                                break;
                            }
                        }
                        else if (key.getNodeValue().equals("ref")) {
                            var stationNumber = attr.getNamedItem("v").getNodeValue();
                            OSMStation stationOSMNode = new OSMStation(osmId, lat, lon, stationNumber);
                            var agent = MasterAgent.tryAddNewStationAgent(stationOSMNode);
                            agent.start();
                        }
                    }
                }
            }
        }

        return busInfos;
    }

    private Optional<BusInfo> parseSingleBusInfo(Node osmNode) {
        List<Long> stationIds = new ArrayList<>();
        String busLine = "";
        StringBuilder builder = new StringBuilder();
        for (var member : IterableNodeList.of(osmNode.getChildNodes())) {
            if (member.getNodeName().equals("member")) {
                NamedNodeMap attributes = member.getAttributes();
                long id = Long.parseLong(attributes.getNamedItem("ref").getNodeValue());
                if (attributes.getNamedItem("role").getNodeValue().contains("stop") &&
                        attributes.getNamedItem("type").getNodeValue().equals("node")) {
                    stationIds.add(id);
                }
                else if (attributes.getNamedItem("role").getNodeValue().length() == 0 &&
                        attributes.getNamedItem("type").getNodeValue().equals("way")) {
                    builder.append(OsmQueryManager.getSingleBusWayOverpassQuery(id));
                }
            }
            else if (member.getNodeName().equals("tag")) {
                NamedNodeMap attributes = member.getAttributes();
                Node namedItemID = attributes.getNamedItem("k");
                if (namedItemID.getNodeValue().equals("ref")) {
                    Node lineNumber = attributes.getNamedItem("v");
                    busLine = lineNumber.getNodeValue();
                }
            }
        }

        List<OSMWay> ways;
        try {
            var overpassNodes =
                    MapAccessManager.getNodesViaOverpass(OsmQueryManager.getQueryWithPayload(builder.toString()));
            ways = MapAccessManager.parseOsmWay(overpassNodes, zone);
        } catch (NoSuchElementException | UnsupportedOperationException e) {
            logger.warn("Please change the zone, this one is not supported yet.", e);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error setting osm way", e);
            return Optional.empty();
        }

        List<Long> filteredStationOsmIds = new ArrayList<>();
        for (Long osmStationId : stationIds) {
            // TODO: Remove
            OSMStation station = MasterAgent.osmIdToStationOSMNode.get(osmStationId);
            if (station != null && zone.contains(station)) {
                filteredStationOsmIds.add(osmStationId);
            }
        }

        return Optional.of(new BusInfo(busLine, ways, filteredStationOsmIds));
    }

    private Collection<BrigadeInfo> generateBrigadeInfos(BusInfo info) {
        Map<String, BrigadeInfo> brigadeNrToBrigadeInfo = new HashMap<>();
        var busLine = info.getBusLine();
        for (OSMStation station : info.getStations()) {
            var query = BusLinesManager.getBusWarszawskieQuery(station.getBusStopId(), station.getBusStopNr(), busLine);
            var nodesOptional = getNodesViaWarszawskie(query);
            nodesOptional.ifPresent(jsonObject -> parseBusInfos(brigadeNrToBrigadeInfo, station, jsonObject));
        }

        return brigadeNrToBrigadeInfo.values();
    }

    private Optional<JSONObject> getNodesViaWarszawskie(String query) {
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

    private void parseBusInfos(Map<String, BrigadeInfo> brigadeNrToBrigadeInfo, OSMStation station, JSONObject jsonObject) {
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
