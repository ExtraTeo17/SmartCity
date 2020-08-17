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

public class BusLinesManager implements IBusLinesManager {
    private static final Logger logger = LoggerFactory.getLogger(BusLinesManager.class);
    private static final JSONParser jsonParser = new JSONParser();
    private static final int BUS_PREPARATION_STEPS = 5;
    private final IZone zone;

    @Inject
    BusLinesManager(IZone zone) {
        this.zone = zone;
    }

    // TODO: CreateBusFunc - Temporary
    @Override
    public Set<BusInfo> getBusInfos() {
        logger.info("STEP 1/" + BUS_PREPARATION_STEPS + ": Starting bus preparation");
        Set<BusInfo> busInfoSet = new HashSet<>();
        try {
            logger.info("STEP 2/" + BUS_PREPARATION_STEPS + ": Sending bus overpass query");
            // TODO: Move part of the logic from MapAccess manager here
            logger.info("STEP 3/" + BUS_PREPARATION_STEPS + ": Starting overpass bus info parsing");
            busInfoSet = sendBusOverpassQuery();
        } catch (Exception e) {
            logger.error("Error sending bus overpass query");
            throw new RuntimeException(e);
        }

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

    private Set<BusInfo> sendBusOverpassQuery() {
        Set<BusInfo> busInfos;
        var overpassQuery = OsmQueryManager.getBusOverpassQuery(zone.getCenter(), zone.getRadius());
        try {
            var overpassInfo = MapAccessManager.getNodesViaOverpass(overpassQuery);
            busInfos = parseBusInfo(overpassInfo);
        } catch (Exception e) {
            logger.warn("Error getting bus info.", e);
            throw new RuntimeException(e);
        }

        return busInfos;
    }

    private Set<BusInfo> parseBusInfo(Document nodesViaOverpass) {
        Node osmRoot = nodesViaOverpass.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        Set<BusInfo> busInfos = new LinkedHashSet<>();
        // TODO: Starts from 1, intended?
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            Node osmNode = osmXMLNodes.item(i);
            if (osmNode.getNodeName().equals("relation")) {
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
                    throw new IllegalArgumentException(e);
                } catch (Exception e) {
                    logger.error("Error setting osm way", e);
                    throw new IllegalArgumentException(e);
                }

                List<Long> filteredStationOsmIds = new ArrayList<>();
                for (Long osmStationId : stationIds) {
                    OSMStation station = MasterAgent.osmIdToStationOSMNode.get(osmStationId);
                    if (station != null && zone.contains(station)) {
                        filteredStationOsmIds.add(osmStationId);
                    }
                }
                var info = new BusInfo(busLine, ways, filteredStationOsmIds);
                busInfos.add(info);
            }
            else if (osmNode.getNodeName().equals("node")) {
                NamedNodeMap attributes = osmNode.getAttributes();
                String osmId = attributes.getNamedItem("id").getNodeValue();
                String lat = attributes.getNamedItem("lat").getNodeValue();
                String lon = attributes.getNamedItem("lon").getNodeValue();

                if (zone.contains(Position.of(lat, lon)) &&
                        !MasterAgent.osmIdToStationOSMNode.containsKey(Long.parseLong(osmId))) {
                    NodeList list_tags = osmNode.getChildNodes();
                    for (int z = 0; z < list_tags.getLength(); z++) {
                        Node tag = list_tags.item(z);
                        if (tag.getNodeName().equals("tag")) {
                            NamedNodeMap attr = tag.getAttributes();
                            Node kAttr = attr.getNamedItem("k");
                            if (kAttr.getNodeValue().equals("public_transport")) {
                                Node vAttr = attr.getNamedItem("v");
                                if (!vAttr.getNodeValue().contains("stop")) {
                                    break;
                                }
                            }
                            else if (kAttr.getNodeValue().equals("ref")) {
                                Node number_of_station = attr.getNamedItem("v");
                                OSMStation stationOSMNode = new OSMStation(osmId, lat, lon, number_of_station.getNodeValue());
                                var agent = MasterAgent.tryAddNewStationAgent(stationOSMNode);
                                agent.start();
                            }
                        }
                    }
                }
            }

        }

        return busInfos;
    }

    private Collection<BrigadeInfo> generateBrigadeInfos(BusInfo info) {
        Map<String, BrigadeInfo> brigadeNrToBrigadeInfo = new HashMap<>();
        var busLine = info.getBusLine();
        for (OSMStation station : info.getStations()) {
            var query = getBusWarszawskieQuery(station.getBusStopId(), station.getBusStopNr(), busLine);
            var nodesOptional = getNodesViaWarszawskie(query);
            nodesOptional.ifPresent(jsonObject -> parseBusInfo(brigadeNrToBrigadeInfo, station, jsonObject));
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

    private void parseBusInfo(Map<String, BrigadeInfo> brigadeNrToBrigadeInfo, OSMStation station, JSONObject jsonObject) {
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
