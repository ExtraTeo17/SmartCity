package osmproxy.buses;

import com.google.common.collect.Sets;
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
import osmproxy.elements.OSMElement;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;
import routing.core.IZone;
import routing.core.Position;
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
    public BusPreparationData getBusData() {
        logger.info("STEP 1/" + BUS_PREPARATION_STEPS + ": Starting bus preparation");
        var overpassInfo = getBusDataXml();
        if (overpassInfo.isEmpty()) {
            return new BusPreparationData();
        }

        var busInfoData = parseBusData(overpassInfo.get());

        for (var busInfo : busInfoData.busInfos) {
            var brigadeInfos = generateBrigadeInfos(busInfo.getBusLine(), busInfo.getStops());
            busInfo.setBrigadeList(brigadeInfos);
        }

        return busInfoData;
    }

    private Optional<Document> getBusDataXml() {
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

    // TODO: Add tests for this function
    private BusPreparationData parseBusData(Document nodesViaOverpass) {
        Node osmRoot = nodesViaOverpass.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();

        Set<BusInfoData> busInfoDataSet = new LinkedHashSet<>();
        HashMap<Long, OSMStation> busStopsMap = new LinkedHashMap<>();

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
                busInfoDataSet.add(busInfo.get());
            }
            else if (nodeName.equals("node")) {
                NamedNodeMap attributes = osmNode.getAttributes();
                long osmId = Long.parseLong(attributes.getNamedItem("id").getNodeValue());
                double lat = Double.parseDouble(attributes.getNamedItem("lat").getNodeValue());
                double lon = Double.parseDouble(attributes.getNamedItem("lon").getNodeValue());

                if (!busStopsMap.containsKey(osmId) && zone.contains(Position.of(lat, lon))) {
                    var stationNumber = searchForStationNumber(osmNode.getChildNodes());
                    stationNumber.ifPresent(s -> busStopsMap.put(osmId, new OSMStation(osmId, lat, lon, s)));
                }
            }
        }

        var busInfos = new LinkedHashSet<BusInfo>();
        var busStopsSet = new LinkedHashSet<>(busStopsMap.values());
        for (var busInfoData : busInfoDataSet) {
            var allBusStops = busInfoData.busStopIds.stream().map(OSMElement::of).collect(Collectors.toSet());
            var validBusStops = Sets.intersection(busStopsSet, allBusStops);
            var info = busInfoData.busInfo;
            info.setStops(validBusStops);
            busInfos.add(info);
        }

        return new BusPreparationData(busInfos, busStopsMap);
    }

    private static class BusInfoData {
        public final BusInfo busInfo;
        public final List<Long> busStopIds;

        private BusInfoData(BusInfo busInfo, List<Long> busStopIds) {
            this.busInfo = busInfo;
            this.busStopIds = busStopIds;
        }
    }

    private Optional<BusInfoData> parseSingleBusInfo(Node osmNode) {
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

        return Optional.of(new BusInfoData(new BusInfo(busLine, ways), stationIds));
    }

    private Optional<String> searchForStationNumber(NodeList nodes) {
        return IterableNodeList.of(nodes)
                .stream()
                .filter(n -> n.getNodeName().equals("tag"))
                .map(Node::getAttributes)
                .dropWhile(attr -> !attr.getNamedItem("k").getNodeValue().equals("public_transport"))
                .filter(attr -> attr.getNamedItem("k").getNodeValue().equals("ref"))
                .findFirst()
                .map(attr -> attr.getNamedItem("v").getNodeValue());
    }

    private Collection<BrigadeInfo> generateBrigadeInfos(String busLine, Collection<OSMStation> osmStations) {
        Map<String, BrigadeInfo> brigadeNrToBrigadeInfo = new HashMap<>();
        for (OSMStation station : osmStations) {
            var query = BusLinesManager.getBusWarszawskieQuery(station.getBusStopId(), station.getBusStopNr(), busLine);
            var nodesOptional = getNodesViaWarszawskieAPI(query);
            var stationId = station.getId();
            nodesOptional.ifPresent(jsonObject -> {
                JSONArray msg = (JSONArray) jsonObject.get("result");
                BrigadeInfo lastInfo = null;
                for (Object o : msg) {
                    JSONObject jsonObj = (JSONObject) o;
                    JSONArray valuesArray = (JSONArray) jsonObj.get("values");
                    for (Object item : valuesArray) {
                        JSONObject valueObject = (JSONObject) item;
                        String key = (String) valueObject.get("key");
                        String brigadeNr = (String) valueObject.get("value");
                        if (key.equals("brygada")) {
                            if (!brigadeNrToBrigadeInfo.containsKey(brigadeNr)) {
                                lastInfo = new BrigadeInfo(brigadeNr);
                                brigadeNrToBrigadeInfo.put(brigadeNr, lastInfo);
                            }
                        }
                        else if (key.equals("czas") && lastInfo != null) {
                            lastInfo.addToTimetable(stationId, brigadeNr);
                        }
                    }
                }
            });
        }

        return brigadeNrToBrigadeInfo.values();
    }

    private Optional<JSONObject> getNodesViaWarszawskieAPI(String query) {
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

    private static String getBusWarszawskieQuery(String busStopId, String busStopNr, String busLine) {
        return "https://api.um.warszawa.pl/api/action/dbtimetable_get/?id=e923fa0e-d96c-43f9-ae6e-60518c9f3238&busstopId=" + busStopId + "&busstopNr=" + busStopNr + "&line=" + busLine + "&apikey=400dacf8-9cc4-4d6c-82cc-88d9311401a5";
    }
}
