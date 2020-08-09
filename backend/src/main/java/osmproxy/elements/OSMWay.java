package osmproxy.elements;

import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class OSMWay extends OSMElement {
    private final static Logger logger = LoggerFactory.getLogger(OSMWay.class);
    private final List<String> childNodeIds;
    private boolean isOneWay;
    private List<OSMWaypoint> waypoints;
    private LightOrientation lightOrientation = null;
    private RelationOrientation relationOrientation = null;
    private RouteOrientation routeOrientation = RouteOrientation.FRONT;

    public OSMWay(Node item) {
        super(item.getAttributes().getNamedItem("id").getNodeValue());
        waypoints = new ArrayList<>();
        NodeList childNodes = item.getChildNodes();
        for (int k = 0; k < childNodes.getLength(); ++k) {
            Node el = childNodes.item(k);
            if (el.getNodeName().equals("nd")) {
                NamedNodeMap attributes = el.getAttributes();
                String nodeRef = attributes.getNamedItem("ref").getNodeValue();
                double lat = Double.parseDouble(attributes.getNamedItem("lat").getNodeValue());
                double lng = Double.parseDouble(attributes.getNamedItem("lon").getNodeValue());
                addPoint(new OSMWaypoint(nodeRef, lat, lng));
            }
            else if (el.getNodeName().equals("tag") &&
                    el.getAttributes().getNamedItem("k").getNodeValue().equals("oneway")) {
                fillOneWay(el.getAttributes().getNamedItem("v").getNodeValue());
            }
            // TODO: consider adding other tags
        }
        childNodeIds = new ArrayList<>();
    }

    // TODO: Where is the logic here?
    private void fillOneWay(final String nodeValue) {
        if (nodeValue.equals("yes")) {
            isOneWay = true;
        }
        else if (nodeValue.equals("no")) {
            isOneWay = false;
        }
    }

    private void addPoint(final OSMWaypoint waypoint) {
        waypoints.add(waypoint);
    }

    // TODO: Result is not queried anywhere, why are we adding it?
    void addChildNodeId(final String id) {
        childNodeIds.add(id);
    }

    public final List<OSMWaypoint> getWaypoints() {
        return waypoints;
    }

    public final OSMWaypoint getWaypoint(int i) {
        return waypoints.get(i);
    }

    public final int getWaypointCount() {
        return waypoints.size();
    }

    // TODO: isOneWayAnd -> logic shows that isNotOneWay or Sth
    public boolean isOneWayAndLightContiguous(final long osmLightId) {
        return !(isOneWay && Long.parseLong(waypoints.get(0).getOsmNodeRef()) == osmLightId);
    }

    final boolean isLightOriented() {
        return lightOrientation != null;
    }

    public final LightOrientation getLightOrientation() {
        return lightOrientation;
    }

    public final RelationOrientation getRelationOrientation() {
        return relationOrientation;
    }

    public final RouteOrientation getRouteOrientation() {
        return routeOrientation;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder()
                .append(super.toString())
                .append("waypoints:" + "\n");
        for (final OSMWaypoint waypoint : waypoints) {
            builder.append(waypoint.getPosition());
        }
        return builder.append("\n").toString();
    }

    void determineLightOrientationTowardsCrossroad(final String osmLightId) {
        if (waypoints.get(0).getOsmNodeRef().equals(osmLightId)) {
            lightOrientation = LightOrientation.LIGHT_AT_ENTRY;
        }
        else if (waypoints.get(waypoints.size() - 1).getOsmNodeRef().equals(osmLightId)) {
            lightOrientation = LightOrientation.LIGHT_AT_EXIT;
        }
    }

    // TODO: Describe this function - what it is returning?
    // TODO: Return optional instead of throwing
    public String determineRelationOrientation(final String adjacentNodeRef) {
        String firstOsmNodeRef = getNodeReference(0);
        String lastOsmNodeRef = getNodeReference(-1);
        if (firstOsmNodeRef.equals(adjacentNodeRef)) {
            relationOrientation = RelationOrientation.FRONT;
            return lastOsmNodeRef;
        }
        else if (lastOsmNodeRef.equals(adjacentNodeRef)) {
            relationOrientation = RelationOrientation.BACK;
            return firstOsmNodeRef;
        }

        throw new UnsupportedOperationException("This orientation is not yet known :(");
    }


    /**
     * @param index - negative indicates that it should start from end (waypoints.size() - index)
     */
    private String getNodeReference(int index) {
        if (index < 0) {
            index = waypoints.size() - index;
        }
        return waypoints.get(index).getOsmNodeRef();
    }

    // TODO: Describe this function - what it is returning?
    public String determineRelationOrientation(final OSMWay nextWay) {
        String firstWayFirstOsmNodeRef = getNodeReference(0);
        String firstWayLastOsmNodeRef = getNodeReference(-1);
        String secondWayFirstOsmNodeRef = nextWay.getNodeReference(0);
        String secondWayLastOsmNodeRef = nextWay.getNodeReference(-1);
        if (firstWayFirstOsmNodeRef.equals(secondWayFirstOsmNodeRef) ||
                firstWayFirstOsmNodeRef.equals(secondWayLastOsmNodeRef)) {
            relationOrientation = RelationOrientation.BACK;
            return firstWayFirstOsmNodeRef;
        }
        else if (firstWayLastOsmNodeRef.equals(secondWayFirstOsmNodeRef) ||
                firstWayLastOsmNodeRef.equals(secondWayLastOsmNodeRef)) {
            relationOrientation = RelationOrientation.FRONT;
            return firstWayLastOsmNodeRef;
        }

        throw new UnsupportedOperationException("This orientation is not yet known :(");
    }

    public GeoPosition getLightNeighborPos() {
        return switch (lightOrientation) {
            case LIGHT_AT_ENTRY -> waypoints.get(1).getPosition();
            case LIGHT_AT_EXIT -> waypoints.get(waypoints.size() - 1 - 1).getPosition();
        };
    }

    public boolean startsInCircle(int radius, double middleLat, double middleLon) {
        return waypoints.get(0).containedInCircle(radius, middleLat, middleLon);
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

    private static class Siblings<T> {
        public final T first;
        public final T second;

        private Siblings(T first, T second) {
            this.first = first;
            this.second = second;
        }

        private Siblings(T first) {
            this(first, null);
        }

        private boolean isSecondPresent() {
            return second != null;
        }
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

    public enum RelationOrientation {
        BACK,
        FRONT
    }

    public enum RouteOrientation {
        BACK,
        FRONT
    }
}
