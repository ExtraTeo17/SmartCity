package osmproxy.elements;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import routing.core.IGeoPosition;
import routing.core.IZone;
import routing.core.Position;
import utilities.ForSerialization;
import utilities.IterableNodeList;
import utilities.Siblings;

import java.io.Serializable;
import java.util.*;

public class OSMWay extends OSMElement implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(OSMWay.class);
    private final List<String> childNodeIds;
    private final boolean isOneWay;
    private List<OSMWaypoint> waypoints;
    private LightOrientation lightOrientation = null;
    private RouteOrientation routeOrientation = RouteOrientation.FRONT;

    @ForSerialization
    public OSMWay() {
        childNodeIds = new ArrayList<>();
        isOneWay = false;
    }

    public OSMWay(Node item) {
        super(item.getAttributes().getNamedItem("id").getNodeValue());
        this.childNodeIds = new ArrayList<>();

        Boolean oneWayValue = null;
        this.waypoints = new ArrayList<>();
        for (var el : IterableNodeList.of(item.getChildNodes())) {
            if (el.getNodeName().equals("nd")) {
                NamedNodeMap attributes = el.getAttributes();
                String nodeRef = attributes.getNamedItem("ref").getNodeValue();
                double lat = Double.parseDouble(attributes.getNamedItem("lat").getNodeValue());
                double lng = Double.parseDouble(attributes.getNamedItem("lon").getNodeValue());
                this.waypoints.add(new OSMWaypoint(nodeRef, lat, lng));
            }
            else if (el.getNodeName().equals("tag") &&
                    el.getAttributes().getNamedItem("k").getNodeValue().equals("oneway")
                    && oneWayValue == null) {
                String value = el.getAttributes().getNamedItem("v").getNodeValue();
                if (value.equals("yes")) {
                    oneWayValue = true;
                }
                else if (value.equals("no")) {
                    oneWayValue = false;
                }
            }
            // TODO: consider adding other tags
        }
        this.isOneWay = oneWayValue != null ? oneWayValue : false;
    }

    @VisibleForTesting
    OSMWay(long id, final List<OSMWaypoint> waypoints) {
        super(id);
        this.waypoints = waypoints;
        this.childNodeIds = new ArrayList<>();
        this.isOneWay = false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append(super.toString())
                .append(", waypoints:" + "\n");
        for (final OSMWaypoint waypoint : waypoints) {
            builder.append(waypoint).append(", ");
        }
        return builder.append("\n").toString();
    }

    // TODO: Result is not queried anywhere, why are we adding it?
    void addChildNodeId(final String id) {
        childNodeIds.add(id);
    }

    public List<OSMWaypoint> getWaypoints() {
        return Lists.newCopyOnWriteArrayList(waypoints);
    }

    /**
     * @param index Negative indicates that it should start from end (waypoints.size() + index)
     */
    public OSMWaypoint getWaypoint(int index) {
        if (index < 0) {
            return waypoints.get(waypoints.size() + index);
        }
        return waypoints.get(index);
    }

    public OSMWaypoint getStart() {
        return getWaypoint(0);
    }

    public OSMWaypoint getEnd() {
        return getWaypoint(-1);
    }

    public int getWaypointCount() {
        return waypoints.size();
    }

    // TODO: isOneWayAnd -> logic shows that isNotOneWay or Sth
    public boolean isOneWayAndLightContiguous(final long osmLightId) {
        return !(isOneWay && Long.parseLong(waypoints.get(0).getOsmNodeRef()) == osmLightId);
    }

    final boolean isLightOriented() {
        return lightOrientation != null;
    }

    public LightOrientation getLightOrientation() {
        return lightOrientation;
    }

    public RouteOrientation getRouteOrientation() {
        return routeOrientation;
    }

    void determineLightOrientationTowardsCrossroad(final String osmLightId) {
        if (waypoints.get(0).getOsmNodeRef().equals(osmLightId)) {
            lightOrientation = LightOrientation.LIGHT_AT_ENTRY;
        }
        else if (waypoints.get(waypoints.size() - 1).getOsmNodeRef().equals(osmLightId)) {
            lightOrientation = LightOrientation.LIGHT_AT_EXIT;
        }
    }

    /**
     * @param adjacentWaypoint Last waypoint on the previous way
     * @return Last nodeReference on the current way
     */
    public Optional<String> reverseTowardsNode(OSMWaypoint adjacentWaypoint) {
        return reverseTowardsNode(adjacentWaypoint.getOsmNodeRef());
    }

    /**
     * @param adjacentNodeRef Last node on the previous way
     * @return Last node on the current way
     */
    public Optional<String> reverseTowardsNode(String adjacentNodeRef) {
        String firstOsmNodeRef = getNodeReference(0);
        String lastOsmNodeRef = getNodeReference(-1);
        String result = null;
        if (firstOsmNodeRef.equals(adjacentNodeRef)) {
            result = lastOsmNodeRef;
        }
        else if (lastOsmNodeRef.equals(adjacentNodeRef)) {
            Collections.reverse(waypoints);
            result = firstOsmNodeRef;
        }

        return Optional.ofNullable(result);
    }

    /**
     * @param index - negative indicates that it should start from end (waypoints.size() + index)
     */
    private String getNodeReference(int index) {
        return getWaypoint(index).getOsmNodeRef();
    }


    /**
     * @param other OsmWay connected with currentWay
     * @return lastNodeRef of other way after orientation
     */
    public String orientateWith(OSMWay other) {
        Optional<String> nodeRef;
        var end = getEnd();
        if (endIsConnectedTo(other)) {
            nodeRef = other.reverseTowardsNode(end);
        }
        else { // startIsConnectedTo(second)
            nodeRef = reverseTowardsNode(end);
            if (nodeRef.isPresent()) {
                nodeRef = other.reverseTowardsNode(nodeRef.get());
            }
        }

        if (nodeRef.isEmpty()) {
            throw new IllegalStateException("Way + " + other + " + is not connected to " + this);
        }

        return nodeRef.get();
    }

    public Position getLightNeighborPos() {
        return switch (lightOrientation) {
            case LIGHT_AT_ENTRY -> waypoints.get(1);
            case LIGHT_AT_EXIT -> waypoints.get(waypoints.size() - 2);
        };
    }


    /**
     * @return Starts or ends in zone
     */
    public boolean isInZone(IZone zone) {
        return startsInZone(zone) || endsInZone(zone);
    }

    public boolean startsInZone(IZone zone) {
        return zone.contains(getWaypoint(0));
    }

    public boolean endsInZone(IZone zone) {
        return zone.contains(getWaypoint(-1));
    }

    /**
     * @return Starts or ends with same Waypoint as other
     */
    @Contract(pure = true)
    public boolean isConnectedTo(final OSMWay other) {
        return startIsConnectedTo(other) || endIsConnectedTo(other);
    }

    @Contract(pure = true)
    public boolean startIsConnectedTo(final OSMWay other) {
        return startsWith(other.getStart()) || startsWith(other.getEnd());
    }

    @Contract(pure = true)
    public boolean startsWith(OSMWaypoint waypoint) {
        return getStart().equals(waypoint);
    }

    @Contract(pure = true)
    public boolean endIsConnectedTo(final OSMWay other) {
        return endsWith(other.getStart()) || endsWith(other.getEnd());
    }

    @Contract(pure = true)
    public boolean endsWith(OSMWaypoint waypoint) {
        return getEnd().equals(waypoint);
    }

    // TODO: What is returned here?
    public int determineRouteOrientationAndFilterRelevantNodes(OSMWay other, String startingNodeRef) {
        var startingNodeIndex = indexOf(startingNodeRef);
        return determineRouteOrientationAndFilterRelevantNodes(other, startingNodeIndex.orElse(waypoints.size() - 1));
    }

    public int determineRouteOrientationAndFilterRelevantNodes(OSMWay other, int startingNodeIndex) {
        if (other == null) {
            throw new IllegalArgumentException("Other way cannot be null");
        }

        var tangentialNodes = getTangentialNodes(other);
        var firstRange = tangentialNodes.first;
        if (tangentialNodes.isSecondPresent()) {
            var secondRange = tangentialNodes.second;
            if (distance(startingNodeIndex, secondRange.from) < distance(startingNodeIndex, firstRange.from)) {
                determineRouteOrientationAndFilterRelevantNodes(startingNodeIndex, secondRange.from);
                return secondRange.to;
            }
        }

        determineRouteOrientationAndFilterRelevantNodes(startingNodeIndex, firstRange.from);
        return firstRange.to;
    }

    private Siblings<Range> getTangentialNodes(OSMWay other) {
        Range firstRange = null;
        for (int i = 0; i < other.waypoints.size(); ++i) {
            for (int j = 0; j < waypoints.size(); ++j) {
                if (other.getNodeReference(i).equals(getNodeReference(j))) {
                    if (firstRange == null) {
                        firstRange = new Range(j, i);
                    }
                    else {
                        return new Siblings<>(firstRange, new Range(j, i));
                    }
                }
            }
        }

        if (firstRange != null) {
            return new Siblings<>(firstRange);
        }

        throw new NoSuchElementException("No nodes were found");
    }

    private void determineRouteOrientationAndFilterRelevantNodes(int startingNodeIndex, int finishingNodeIndex) {
        // TODO: Delete it - just pass correct parameters and determine orientation in other function
        determineRouteOrientation(startingNodeIndex, finishingNodeIndex);
        if (routeOrientation == RouteOrientation.BACK) {
            waypoints = waypoints.subList(finishingNodeIndex, startingNodeIndex + 1);
        }
        else {
            waypoints = waypoints.subList(startingNodeIndex, finishingNodeIndex + 1);
        }
    }

    public void determineRouteOrientationAndFilterRelevantNodes(int startingNodeIndex, String finishingNodeRef) {
        var finishingNodeIndex = indexOf(finishingNodeRef);
        determineRouteOrientationAndFilterRelevantNodes(startingNodeIndex, finishingNodeIndex.orElse(0));
    }

    private void determineRouteOrientation(int start, int end) {
        if (start > end) {
            routeOrientation = RouteOrientation.BACK;
        }
    }

    private int distance(int index1, int index2) {
        return Math.abs(index1 - index2);
    }

    private Optional<Integer> indexOf(final String osmNodeRef) {
        for (int i = 0; i < waypoints.size(); ++i) {
            if (getNodeReference(i).equals(osmNodeRef)) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    private static class Range {
        public final int from;
        public final int to;

        private Range(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

    // TODO: Move enums to other file or make it package private
    public enum LightOrientation {
        LIGHT_AT_ENTRY,
        LIGHT_AT_EXIT
    }

    public enum RouteOrientation {
        BACK,
        FRONT
    }

	public String findClosestNodeRefTo(IGeoPosition pointA) {
		String closestNodeRef = null;
		double minDist = Double.MAX_VALUE;
		for (final OSMWaypoint point : waypoints) {
			double currDist = point.distance(pointA);
			if (currDist < minDist) {
				minDist = currDist;
				closestNodeRef = point.getOsmNodeRef();
			}
		}
		return closestNodeRef;
	}

	// TODO: NEW function -- in case of new bugs start debugging here
	public void determineRouteOrientationAndFilterRelevantNodes(String startingOsmNodeRef, String finishingOsmNodeRef) {
		Optional<Integer> startingIndex = indexOf(startingOsmNodeRef);
		Optional<Integer> finishingIndex = indexOf(finishingOsmNodeRef);
		determineRouteOrientationAndFilterRelevantNodes(
				startingIndex.orElseThrow(() -> new IllegalArgumentException(
						"Starting OSM node ref: " + startingOsmNodeRef + " was not on way: " + getId())),
				finishingIndex.orElseThrow(() -> new IllegalArgumentException(
						"Starting OSM node ref: " + startingOsmNodeRef + " was not on way: " + getId())));
	}
}
