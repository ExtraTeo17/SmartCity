package osmproxy.buses;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.json.simple.JSONObject;
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
import routing.core.IZone;
import routing.core.Position;
import utilities.IterableJsonArray;
import utilities.IterableNodeList;

import java.util.*;
import java.util.function.Predicate;

public class BusLinesManager implements IBusLinesManager {
    private static final Logger logger = LoggerFactory.getLogger(BusLinesManager.class);

    private final IBusApiManager busApiManager;
    private final IZone zone;

    @Inject
    BusLinesManager(IBusApiManager busApiManager, IZone zone) {
        this.busApiManager = busApiManager;
        this.zone = zone;
    }

    @Override
    public BusPreparationData getBusData() {
        var overpassInfo = busApiManager.getBusDataXml(zone);
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

    // TODO: Add tests for this function
    private BusPreparationData parseBusData(Document busData) {
        Node osmRoot = busData.getFirstChild();
        var osmXMLNodes = IterableNodeList.of(osmRoot.getChildNodes());

        Set<BusInfoData> busInfoDataSet = new LinkedHashSet<>();
        HashMap<Long, OSMStation> busStopsMap = new LinkedHashMap<>();
        int errors = 0;
        for (var osmNode : Iterables.skip(osmXMLNodes, 1)) {
            var nodeName = osmNode.getNodeName();
            if (nodeName.equals("relation")) {
                var busInfo = parseRelation(osmNode);
                if (busInfo.isEmpty()) {
                    if (++errors < 5) {
                        continue;
                    }
                    throw new RuntimeException("Too much errors when parsing busInfo");
                }
                busInfoDataSet.add(busInfo.get());
            }
            else if (nodeName.equals("node")) {
                var station = parseNode(osmNode, busStopsMap::containsKey);
                station.ifPresent(s -> busStopsMap.put(s.getId(), s));
            }
        }

        var busInfos = getBusInfosWithStops(busInfoDataSet, busStopsMap);

        return new BusPreparationData(busInfos, busStopsMap);
    }

    @VisibleForTesting
    static class BusInfoData {
        private final BusInfo busInfo;
        private final List<Long> busStopIds;

        BusInfoData(BusInfo busInfo, List<Long> busStopIds) {
            this.busInfo = busInfo;
            this.busStopIds = busStopIds;
        }
    }

    private Optional<BusInfoData> parseRelation(Node relation) {
        List<Long> stationIds = new ArrayList<>();
        String busLine = "";
        StringBuilder busWayQueryBuilder = new StringBuilder();
        for (var member : IterableNodeList.of(relation.getChildNodes())) {
            if (member.getNodeName().equals("member")) {
                NamedNodeMap attributes = member.getAttributes();
                long id = Long.parseLong(attributes.getNamedItem("ref").getNodeValue());
                if (attributes.getNamedItem("role").getNodeValue().contains("stop") &&
                        attributes.getNamedItem("type").getNodeValue().equals("node")) {
                    stationIds.add(id);
                }
                else if (attributes.getNamedItem("role").getNodeValue().length() == 0 &&
                        attributes.getNamedItem("type").getNodeValue().equals("way")) {
                    busWayQueryBuilder.append(OsmQueryManager.getSingleBusWayQuery(id));
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
                    MapAccessManager.getNodesViaOverpass(OsmQueryManager.getQueryWithPayload(busWayQueryBuilder.toString()));
            ways = MapAccessManager.parseOsmWays(overpassNodes, zone);
        } catch (NoSuchElementException | UnsupportedOperationException e) {
            logger.warn("Please change the zone, this one is not supported yet.", e);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error setting osm way", e);
            return Optional.empty();
        }

        return Optional.of(new BusInfoData(new BusInfo(busLine, ways), stationIds));
    }

    private Optional<OSMStation> parseNode(Node node, Predicate<Long> isPresent) {
        NamedNodeMap attributes = node.getAttributes();
        long osmId = Long.parseLong(attributes.getNamedItem("id").getNodeValue());
        double lat = Double.parseDouble(attributes.getNamedItem("lat").getNodeValue());
        double lon = Double.parseDouble(attributes.getNamedItem("lon").getNodeValue());

        boolean isPresentVal = isPresent.test(osmId);
        if (!isPresentVal && zone.contains(Position.of(lat, lon))) {
            var stationNumber = searchForStationNumber(node.getChildNodes());
            if (stationNumber.isPresent()) {
                return Optional.of(new OSMStation(osmId, lat, lon, stationNumber.get()));
            }
        }
        else {
            logger.debug("Station: " + osmId + " won't be included. IsPresent: " + isPresentVal);
        }

        return Optional.empty();
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
        Map<String, BrigadeInfo> brigadeNrToBrigadeInfo = new LinkedHashMap<>();
        for (OSMStation station : osmStations) {
            var nodesOptional = busApiManager.getNodesViaWarszawskieAPI(station.getBusStopId(),
                    station.getBusStopNr(), busLine);
            nodesOptional.ifPresent(jsonObject -> {
                var stationId = station.getId();
                BrigadeInfo lastInfo = null;
                for (JSONObject obj : IterableJsonArray.of(jsonObject, "result")) {
                    for (JSONObject item : IterableJsonArray.of(obj, "values")) {
                        String key = (String) item.get("key");
                        String brigadeNr = (String) item.get("value");
                        if (key.equals("brygada")) {
                            lastInfo = brigadeNrToBrigadeInfo.get(brigadeNr);
                            if (lastInfo == null) {
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

    @VisibleForTesting
    LinkedHashSet<BusInfo> getBusInfosWithStops(Collection<BusInfoData> busInfoDataSet, Map<Long, OSMStation> busStops) {
        var busInfos = new LinkedHashSet<BusInfo>();
        for (var busInfoData : busInfoDataSet) {
            List<OSMStation> validBusStops = new ArrayList<>(busInfoData.busStopIds.size());
            for (var id : busInfoData.busStopIds) {
                var station = busStops.get(id);
                if (station != null) {
                    // WARN: Station is not copied here - should not be modified in any way
                    validBusStops.add(station);
                }
            }
            var info = busInfoData.busInfo;
            info.setStops(validBusStops);
            busInfos.add(info);
        }

        return busInfos;
    }
}
