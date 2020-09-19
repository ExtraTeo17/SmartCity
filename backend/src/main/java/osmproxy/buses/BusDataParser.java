package osmproxy.buses;

import com.google.inject.Inject;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import osmproxy.buses.abstractions.IBusApiManager;
import osmproxy.buses.abstractions.IBusDataParser;
import osmproxy.buses.abstractions.IDataMerger;
import osmproxy.buses.data.BusInfoData;
import osmproxy.buses.data.BusPreparationData;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;
import routing.core.IZone;
import routing.core.Position;
import utilities.IterableNodeList;

import java.util.*;
import java.util.function.Predicate;

public class BusDataParser implements IBusDataParser {
    private static final Logger logger = LoggerFactory.getLogger(BusDataParser.class);

    private final IDataMerger busDataMerger;
    private final IBusApiManager busApiManager;
    private final IZone zone;

    @Inject
    BusDataParser(IDataMerger busDataMerger,
                  IBusApiManager busApiManager,
                  IZone zone) {
        this.busDataMerger = busDataMerger;
        this.busApiManager = busApiManager;
        this.zone = zone;
    }

    // TODO: Add tests for this function
    @Override
    public BusPreparationData parseBusData(Document busData) {
        Node osmRoot = busData.getFirstChild();
        var osmXMLNodes = IterableNodeList.of(osmRoot.getChildNodes());

        Set<BusInfoData> busInfoDataSet = new LinkedHashSet<>();
        HashMap<Long, OSMStation> busStopsMap = new LinkedHashMap<>();
        int errors = 0;
        for (var osmNode : osmXMLNodes) {
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
                station.ifPresent(st -> busStopsMap.put(st.getId(), st));
            }
        }

        var busInfos = busDataMerger.getBusInfosWithStops(busInfoDataSet, busStopsMap);

        return new BusPreparationData(busInfos, busStopsMap);
    }

    private Optional<BusInfoData> parseRelation(Node relation) {
        List<Long> stationIds = new ArrayList<>();
        String busLine = "";
        List<Long> wayIds = new ArrayList<>();
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
                    wayIds.add(id);
                }
            }
            else if (node.getNodeName().equals("tag")) {
                NamedNodeMap attributes = node.getAttributes();
                Node namedItemID = attributes.getNamedItem("k");
                if (namedItemID.getNodeValue().equals("ref")) {
                    Node lineNumber = attributes.getNamedItem("v");
                    busLine = lineNumber.getNodeValue();
                }
            }
        }

        var waysDoc = busApiManager.getBusWays(wayIds);
        if (waysDoc.isEmpty()) {
            return Optional.empty();
        }
        List<OSMWay> ways = parseOsmWays(waysDoc.get());

        return Optional.of(new BusInfoData(new BusInfo(busLine, ways), stationIds));
    }

    @Override
    public List<OSMWay> parseOsmWays(Document nodes) {
        List<OSMWay> route = new ArrayList<>();
        Node osmRoot = nodes.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        Pair<OSMWay, String> wayAdjacentNodeRef = determineInitialWayRelOrientation(osmXMLNodes);
        String adjacentNodeRef = wayAdjacentNodeRef.getValue1();
        boolean isFirst = true;
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("way")) {
                OSMWay way;
                if (isFirst) {
                    way = wayAdjacentNodeRef.getValue0();
                    isFirst = false;
                }
                else {
                    way = new OSMWay(item);
                    try {
                        adjacentNodeRef = way.determineRelationOrientation(adjacentNodeRef);
                    } catch (UnsupportedOperationException e) {
                        logger.warn("Failed to determine orientation", e);
                        break;
                    }
                }

                // TODO: CORRECT POTENTIAL BUGS CAUSING ROUTE TO BE CUT INTO PIECES BECAUSE OF RZĄŻEWSKI CASE
                if (way.startsInZone(zone)) {
                    route.add(way);
                }
            }
        }

        return route;
    }

    // TODO: Is it returning orientation of next way or current way?
    private static Pair<OSMWay, String> determineInitialWayRelOrientation(final NodeList osmXMLNodes) {
        OSMWay firstWay = null;
        OSMWay lastWay = null;
        for (int it = 1; it < osmXMLNodes.getLength(); ++it) {
            Node node = osmXMLNodes.item(it);
            if (node.getNodeName().equals("way")) {
                var way = new OSMWay(node);
                if (firstWay == null) {
                    firstWay = way;
                }
                else {
                    lastWay = way;
                    break;
                }
            }
        }

        if (firstWay != null && lastWay != null) {
            var orientation = firstWay.determineRelationOrientation(lastWay);
            return Pair.with(lastWay, orientation);
        }

        throw new NoSuchElementException("Did not find two 'way'-type nodes in provided list.");
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
                logger.debug("Parsing station with number: " + stationNumber.get());
                return Optional.of(new OSMStation(osmId, lat, lon, stationNumber.get()));
            }
        }

        logger.debug("Station: " + osmId + " won't be included. IsPresent: " + isPresentVal);

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
}
