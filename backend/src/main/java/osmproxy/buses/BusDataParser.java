package osmproxy.buses;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import osmproxy.buses.abstractions.IApiSerializer;
import osmproxy.buses.abstractions.IBusApiManager;
import osmproxy.buses.abstractions.IBusDataParser;
import osmproxy.buses.abstractions.IDataMerger;
import osmproxy.buses.data.BusInfoData;
import osmproxy.buses.data.BusPreparationData;
import osmproxy.buses.models.TimetableRecord;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;
import routing.core.IZone;
import routing.core.Position;
import utilities.ConditionalExecutor;
import utilities.IterableNodeList;
import utilities.Siblings;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BusDataParser implements IBusDataParser {
    private static final Logger logger = LoggerFactory.getLogger(BusDataParser.class);

    private final IDataMerger busDataMerger;
    private final IApiSerializer apiSerializer;
    private final IBusApiManager busApiManager;
    private final IZone zone;

    @Inject
    BusDataParser(IDataMerger busDataMerger,
                  IApiSerializer apiSerializer, IBusApiManager busApiManager,
                  IZone zone) {
        this.busDataMerger = busDataMerger;
        this.apiSerializer = apiSerializer;
        this.busApiManager = busApiManager;
        this.zone = zone;
    }

    @Override
    public BusPreparationData parseBusData(Document busData) {
        Node osmRoot = busData.getFirstChild();
        var osmXMLNodes = IterableNodeList.of(osmRoot.getChildNodes());

        Set<BusInfoData> busInfoDataSet = new LinkedHashSet<>();
        HashMap<Long, OSMStation> busStopsMap = new LinkedHashMap<>();
        for (var osmNode : osmXMLNodes) {
            var nodeName = osmNode.getNodeName();
            if (nodeName.equals("relation")) {
                var busInfoData = parseRelation(osmNode);
                if (busInfoData.isEmpty()) {
                    logger.info("Bus info data for relation " + osmNode.getAttributes().getNamedItem("id") +
                            " was empty");
                    continue;
                }
                busInfoDataSet.add(busInfoData.get());
            }
            else if (nodeName.equals("node")) {
                var station = parseNode(osmNode, busStopsMap::containsKey);
                station.ifPresent(st -> busStopsMap.put(st.getId(), st));
            }
        }

        var busInfos = busDataMerger.getBusInfosWithStops(busInfoDataSet, busStopsMap);

        List<BusInfo> busInfosToRemove = new ArrayList<>();
        for (var busInfo : busInfos) {
            if (busInfo.stops.isEmpty()) {
                logger.info("Warning: No stops in the for line " + busInfo.busLine);
                continue;
            }

            var brigadeInfos = generateBrigadeInfos(busInfo.busLine, busInfo.stops);
            if (brigadeInfos.isEmpty()) {
                busInfosToRemove.add(busInfo);
                logger.info("Warning: Timetable for bus line " + busInfo.busLine + " is empty in Warszawskie API. " +
                        "Line will not be considered");
                continue;
            }
            busInfo.addBrigades(brigadeInfos);
        }

        busInfos.removeAll(busInfosToRemove);

        return new BusPreparationData(busInfos, busStopsMap);
    }

    private Optional<BusInfoData> parseRelation(Node relation) {
        List<Long> stationIds = new ArrayList<>();
        String busLine = "";
        List<Long> waysIds = new ArrayList<>();
        boolean ztmWarsaw = false;
        for (var node : IterableNodeList.of(relation.getChildNodes())) {
            if (node.getNodeName().equals("member")) {
                NamedNodeMap attributes = node.getAttributes();
                long id = Long.parseLong(attributes.getNamedItem("ref").getNodeValue());
                if (attributes.getNamedItem("role").getNodeValue().contains("stop") &&
                        attributes.getNamedItem("type").getNodeValue().equals("node")) {
                    stationIds.add(id);
                }
                else if (attributes.getNamedItem("role").getNodeValue().length() == 0 &&
                        attributes.getNamedItem("type").getNodeValue().equals("way")) {
                    waysIds.add(id);
                }
            }
            else if (node.getNodeName().equals("tag")) {
                NamedNodeMap attributes = node.getAttributes();
                Node namedItemID = attributes.getNamedItem("k");

                if (namedItemID.getNodeValue().equals("network")) {
                    ztmWarsaw = isZTMWarsaw(attributes);

                }
                if (namedItemID.getNodeValue().equals("ref")) {
                    Node lineNumber = attributes.getNamedItem("v");
                    busLine = lineNumber.getNodeValue();
                }
            }
        }
        if (!ztmWarsaw) {
            return Optional.empty();
        }

        var waysDoc = busApiManager.getBusWays(waysIds);
        if (waysDoc.isEmpty()) {
            return Optional.empty();
        }
        logger.debug("Started parsing ways: " + waysIds.get(0) + "_" + waysIds.get(waysIds.size() - 1));
        List<OSMWay> ways = parseOsmWays(waysDoc.get());

        return Optional.of(new BusInfoData(new BusInfo(busLine, ways), stationIds));
    }

    private boolean isZTMWarsaw(NamedNodeMap relation) {
        return isZTMWarsaw(relation.getNamedItem("v").getNodeValue());
    }

    private boolean isZTMWarsaw(String network) {
        return network.equals("ZTM Warszawa");
    }

    @SuppressWarnings("FeatureEnvy")
    @Override
    public List<OSMWay> parseOsmWays(Document nodes) {
        List<OSMWay> route = new ArrayList<>();
        Node osmRoot = nodes.getFirstChild();
        var osmNodes = IterableNodeList.of(osmRoot.getChildNodes());
        var nodesIter = osmNodes.iterator();
        var twoFirstWaysOpt = findTwoFirstWaysInZone(nodesIter);
        if (twoFirstWaysOpt.isEmpty()) {
            logger.warn("Didn't find two connected ways in provided zone");
            return route;
        }

        var twoFirstWays = twoFirstWaysOpt.get();
        var firstWay = twoFirstWays.first;
        var secondWay = twoFirstWays.second;

        route.add(firstWay);
        route.add(secondWay);
        String adjacentNodeRef = firstWay.orientateWith(secondWay);
        boolean failedToMatchPreviously = false;

        List<OSMWay> wayList = new ArrayList<>();
        int lastIndexInZone = -1;
        while (nodesIter.hasNext()) {
            Node wayNode = nodesIter.next();
            if (wayNode.getNodeName().equals("way")) {
                var way = new OSMWay(wayNode);
                // TODO: if not accurate enough, consider changing to node.isInZone (node-based)
                if (way.isInZone(zone)) {
                    lastIndexInZone = wayList.size();
                }
                wayList.add(way);
            }
        }

        for (int i = 0; i <= lastIndexInZone; ++i) {
            var way = wayList.get(i);
            var referenceOpt = way.reverseTowardsNode(adjacentNodeRef);
            if (referenceOpt.isEmpty()) {
                logger.debug("Failed to match way: " + way + " with " + adjacentNodeRef);
                failedToMatchPreviously = true;
                continue;
            }
            else if (failedToMatchPreviously) {
                logger.info("Reconnected to way: " + way + " after failed match with " + adjacentNodeRef);
            }

            adjacentNodeRef = referenceOpt.get();
            route.add(way);
        }

        return route;
    }

    private Optional<Siblings<OSMWay>> findTwoFirstWaysInZone(Iterator<Node> nodeIterator) {
        OSMWay firstWay = null;
        OSMWay secondWay = null;
        while (nodeIterator.hasNext() && secondWay == null) {
            Node item = nodeIterator.next();
            if (item.getNodeName().equals("way")) {
                OSMWay way = new OSMWay(item);
                if (firstWay == null) {
                    if (way.isInZone(zone)) {
                        firstWay = way;
                    }
                }
                else if (firstWay.isConnectedTo(way)) {
                    secondWay = way;
                }
            }
        }

        if (secondWay == null) {
            return Optional.empty();
        }

        return Optional.of(new Siblings<>(firstWay, secondWay));
    }

    private Optional<OSMStation> parseNode(Node node, Predicate<Long> isPresent) {
        NamedNodeMap attributes = node.getAttributes();
        long osmId = Long.parseLong(attributes.getNamedItem("id").getNodeValue());
        double lat = Double.parseDouble(attributes.getNamedItem("lat").getNodeValue());
        double lon = Double.parseDouble(attributes.getNamedItem("lon").getNodeValue());

        boolean isPresentVal = isPresent.test(osmId);
        if (!isPresentVal && zone.contains(Position.of(lat, lon))) {
            var numberAndTypeOpt = searchForStationNumberAndType(node.getChildNodes());
            if (numberAndTypeOpt.isPresent()) {
                var numberAndType = numberAndTypeOpt.get();
                var stationNumber = numberAndType.first;
                var type = numberAndType.second;
                var isPlatform = type.equals("platform");

                logger.debug("Parsing station with number: " + stationNumber);
                return Optional.of(new OSMStation(osmId, lat, lon, stationNumber, isPlatform));
            }
        }

        logger.trace("Station: " + osmId + " won't be included. IsPresent: " + isPresentVal);

        return Optional.empty();
    }

    /**
     * @param nodes potential Station node
     * @return (Station node number, type)
     */
    private Optional<Siblings<String>> searchForStationNumberAndType(NodeList nodes) {
        var filteredNodes = IterableNodeList.of(nodes)
                .stream()
                .filter(n -> n.getNodeName().equals("tag"))
                .map(Node::getAttributes)
                .collect(Collectors.toList());

        if (filteredNodes.isEmpty()) {
            return Optional.empty();
        }

        boolean isZtmWarsaw = false;
        String type = null;
        String nodeNumber = null;
        for (var nodesMap : filteredNodes) {
            var key = nodesMap.getNamedItem("k").getNodeValue();
            var value = nodesMap.getNamedItem("v").getNodeValue();
            switch (key) {
                case "network" -> isZtmWarsaw = isZTMWarsaw(value);
                case "ref" -> nodeNumber = value;
                case "public_transport" -> type = value;
            }
        }

        if (type == null || nodeNumber == null || !isZtmWarsaw) {
            return Optional.empty();
        }

        return Optional.of(Siblings.of(nodeNumber, type));
    }

    private Collection<BrigadeInfo> generateBrigadeInfos(String busLine, Collection<OSMStation> osmStations) {
        Map<String, BrigadeInfo> brigadeInfoMap = new LinkedHashMap<>();
        for (OSMStation station : osmStations) {
            var jsonStringOpt = busApiManager.getBusTimetablesViaWarszawskieAPI(station.getBusStopId(),
                    station.getBusStopNr(), busLine);
            if (jsonStringOpt.isEmpty()) {
                continue;
            }

            var jsonString = jsonStringOpt.get();
            var timetableRecords = apiSerializer.serializeTimetables(jsonString);
            var brigadeIdToRecords = timetableRecords.stream()
                    .collect(Collectors.groupingBy(record -> record.brigadeId));
            var stationId = station.getId();
            for (var entry : brigadeIdToRecords.entrySet()) {
                var brigadeId = entry.getKey();
                var brigadeTimeRecords = entry.getValue();
                var brigadeInfo = brigadeInfoMap.get(brigadeId);
                if (brigadeInfo == null) {
                    brigadeInfoMap.put(brigadeId, new BrigadeInfo(brigadeId, stationId, brigadeTimeRecords));
                }
                else {
                    brigadeInfo.addTimetableRecords(stationId, brigadeTimeRecords);
                }

                ConditionalExecutor.trace(() -> logBrigadeData(brigadeTimeRecords, station, busLine));
            }
        }

        return brigadeInfoMap.values();
    }

    private void logBrigadeData(List<TimetableRecord> timetableRecords, OSMStation station, String busLine) {
        var recordsString = Joiner.on(" \n").join(timetableRecords);
        logger.info("Printing data for brigade " + timetableRecords.get(0).brigadeId + " :\n" +
                "  times:\n" + recordsString + "\n" +
                "  stationId: " + station.getId() + "\n" +
                "  busStopId: " + station.getBusStopId() + "\n" +
                "  busStopNr: " + station.getBusStopNr() + "\n" +
                "  busLine: " + busLine + "\n");
    }
}
