package smartcity.lights.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import osmproxy.elements.OSMWaypoint;
import routing.RoutingConstants;
import routing.core.IGeoPosition;
import routing.core.Position;
import smartcity.lights.abstractions.ICrossroadParser;
import smartcity.lights.core.data.LightInfo;
import utilities.IterableNodeList;
import utilities.Siblings;

import java.util.ArrayList;
import java.util.List;

public class CrossroadParser implements ICrossroadParser {
    private static final Logger logger = LoggerFactory.getLogger(CrossroadParser.class);
    private static final double COSINE_OF_135_DEGREES = -0.7071;
    private static final double DISTANCE_THRESHOLD = 10 * RoutingConstants.DEGREES_PER_METER;

    @Override
    public Siblings<List<LightInfo>> getLightGroups(OSMNode crossroadCenter) {
        var firstLightGroupInfo = new ArrayList<LightInfo>();
        var secondLightGroupInfo = new ArrayList<LightInfo>();

        var waysIter = crossroadCenter.getParentWaysIterator();
        if (!waysIter.hasNext()) {
            logger.warn("Empty node: " + crossroadCenter.getId());
            return new Siblings<>(firstLightGroupInfo, secondLightGroupInfo);
        }

        var firstWay = waysIter.next();
        var lightInfo = parseLightInfo(firstWay, crossroadCenter);
        firstLightGroupInfo.add(lightInfo);

        var firstWayNeighborPos = firstWay.getLightNeighborPos();
        while (waysIter.hasNext()) {
            OSMWay parentWay = waysIter.next();
            var nextWayLightNeighborPos = parentWay.getLightNeighborPos();
            double cosineCenterNodeNextWay = crossroadCenter.cosineAngle(firstWayNeighborPos, nextWayLightNeighborPos);
            lightInfo = parseLightInfo(parentWay, crossroadCenter);
            if (cosineCenterNodeNextWay < COSINE_OF_135_DEGREES) {
                firstLightGroupInfo.add(lightInfo);
            }
            else {
                secondLightGroupInfo.add(lightInfo);
            }
        }

        return new Siblings<>(firstLightGroupInfo, secondLightGroupInfo);
    }

    private LightInfo parseLightInfo(OSMWay adjacentOsmWay,
                                     OSMNode centerCrossroadNode) {
        var waypointsPair = getNextWaypoints(adjacentOsmWay);
        var adjacentCrossingOsmId1 = waypointsPair.first.getOsmNodeRef();
        var adjacentCrossingOsmId2 = waypointsPair.isSecondPresent() ? waypointsPair.second.getOsmNodeRef() : null;
        var adjacentOsmWayId = adjacentOsmWay.getId();

        var osmLightId = centerCrossroadNode.getId();
        var position = getLightPosition(waypointsPair.first,
                adjacentOsmWay.getLightNeighborPos(),
                centerCrossroadNode);

        return new LightInfo(osmLightId, adjacentOsmWayId, position, adjacentCrossingOsmId1, adjacentCrossingOsmId2);
    }

    private Siblings<OSMWaypoint> getNextWaypoints(OSMWay way) {
        OSMWaypoint secondWaypoint = null;
        OSMWaypoint thirdWaypoint = null;
        int waypointsCount = way.getWaypointCount();
        boolean isWayLongerThan2 = waypointsCount > 2;
        switch (way.getLightOrientation()) {
            case LIGHT_AT_ENTRY -> {
                secondWaypoint = way.getWaypoint(1);
                thirdWaypoint = isWayLongerThan2 ? way.getWaypoint(2) : null;
            }
            case LIGHT_AT_EXIT -> {
                secondWaypoint = way.getWaypoint(waypointsCount - 2);
                thirdWaypoint = isWayLongerThan2 ? way.getWaypoint(waypointsCount - 3) : null;
            }
        }

        return Siblings.of(secondWaypoint, thirdWaypoint);
    }

    private IGeoPosition getLightPosition(IGeoPosition initialPosition,
                                          IGeoPosition lightNeighbourPosition,
                                          IGeoPosition crossroadCenter) {
        var finalPosition = initialPosition;
        var distToCrossroad = lightNeighbourPosition.distance(crossroadCenter);
        if (distToCrossroad > DISTANCE_THRESHOLD) {
            finalPosition = finalPosition.midpoint(crossroadCenter);
            finalPosition = finalPosition.midpoint(crossroadCenter);
        }

        return finalPosition;
    }

    @Override
    public Siblings<List<LightInfo>> getLightGroups(Node crossroad) {
        var crossroadChildren = crossroad.getChildNodes();
        var childrenANode = crossroadChildren.item(1);
        var childrenBNode = crossroadChildren.item(3);

        var lightGroupA = parseInfosList(childrenANode.getChildNodes());
        var lightGroupB = parseInfosList(childrenBNode.getChildNodes());

        return new Siblings<>(lightGroupA, lightGroupB);
    }

    private List<LightInfo> parseInfosList(NodeList lightNodes) {
        var lightInfos = new ArrayList<LightInfo>();
        for (var node : IterableNodeList.of(lightNodes)) {
            if (node.getNodeName().equals("light")) {
                var info = parseLightInfo(node.getAttributes());
                lightInfos.add(info);
            }
        }

        return lightInfos;
    }

    private LightInfo parseLightInfo(NamedNodeMap attributes) {
        var osmId = Long.parseLong(attributes.getNamedItem("light").getNodeValue());
        var lat = Double.parseDouble((attributes.getNamedItem("lat").getNodeValue()));
        var lng = Double.parseDouble((attributes.getNamedItem("lon").getNodeValue()));
        var adjacentOsmWayId = Long.parseLong((attributes.getNamedItem("way").getNodeValue()));
        // TODO: Retrieve crossings!
        return new LightInfo(osmId, adjacentOsmWayId, Position.of(lat, lng), null, null);
    }
}
