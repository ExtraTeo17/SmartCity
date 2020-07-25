package osmproxy.elements;

import org.javatuples.Pair;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class OSMWay extends OSMElement {

    private static final String YES = "yes";
    private static final String NO = "no";
    private final List<String> childNodeIds;
    private List<OSMWaypoint> waypoints;
    private LightOrientation lightOrientation = null;
    private RelationOrientation relationOrientation = null;
    private RouteOrientation routeOrientation = RouteOrientation.FRONT;
    private boolean isOneWay;
    public OSMWay(final String id) {
        super(id);
        waypoints = new ArrayList<>();
        childNodeIds = new ArrayList<>();
    }
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
            // consider adding other tags
        }
        childNodeIds = new ArrayList<>();
    }

    public boolean isOneWayAndLightContiguous(final long osmLightId) {
        return !(isOneWay && Long.parseLong(waypoints.get(0).getOsmNodeRef()) == osmLightId);
    }

    private void fillOneWay(final String nodeValue) {
        if (nodeValue.equals(YES)) {
            isOneWay = true;
        }
        else if (nodeValue.equals(NO)) {
            isOneWay = false;
        }
    }

    public void addPoint(final OSMWaypoint waypoint) {
        waypoints.add(waypoint);
    }

    public void addChildNodeId(final String id) {
        childNodeIds.add(id);
    }

    public final List<OSMWaypoint> getWaypoints() {
        return waypoints;
    }

    public final OSMWaypoint getWaypoint(int i) {
        return waypoints.get(i);
    }

    public final OSMWaypoint getFirstWaypoint() {
        return getWaypoint(0);
    }

    public final OSMWaypoint getLastWaypoint() {
        return getWaypoint(getWaypointCount() - 1);
    }

    public final int getWaypointCount() {
        return waypoints.size();
    }

    public final boolean isLightOriented() {
        return lightOrientation != null;
    }

    public final boolean isRelationOriented() {
        return relationOrientation != null;
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

    public void determineLightOrientationTowardsCrossroad(final String osmLightId) {
        if (waypoints.get(0).getOsmNodeRef().equals(osmLightId)) {
            lightOrientation = LightOrientation.LIGHT_AT_ENTRY;
        }
        else if (waypoints.get(waypoints.size() - 1).getOsmNodeRef().equals(osmLightId)) {
            lightOrientation = LightOrientation.LIGHT_AT_EXIT;
        }
    }

    public String determineRelationOrientation(final String adjacentNodeRef) {
        String firstWayFirstOsmNodeRef = getWaypoint(0).getOsmNodeRef();
        String firstWayLastOsmNodeRef = getWaypoint(getWaypointCount() - 1).getOsmNodeRef();
        if (firstWayFirstOsmNodeRef.equals(adjacentNodeRef)) {
            relationOrientation = RelationOrientation.FRONT;
            return firstWayLastOsmNodeRef;
        }
        else if (firstWayLastOsmNodeRef.equals(adjacentNodeRef)) {
            relationOrientation = RelationOrientation.BACK;
            return firstWayFirstOsmNodeRef;
        }
        else {
            try {
                throw new Exception("This orientation is not yet known :(");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public String determineRelationOrientation(final OSMWay nextWay) {
        String firstWayFirstOsmNodeRef = getWaypoint(0).getOsmNodeRef();
        String firstWayLastOsmNodeRef = getWaypoint(getWaypointCount() - 1).getOsmNodeRef();
        String secondWayFirstOsmNodeRef = nextWay.getWaypoint(0).getOsmNodeRef();
        String secondWayLastOsmNodeRef = nextWay.getWaypoint(nextWay.getWaypointCount() - 1).getOsmNodeRef();
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
        else {
            try {
                throw new Exception("This orientation is not yet known :(");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public GeoPosition getLightNeighborPos() {
        switch (lightOrientation) {
            case LIGHT_AT_ENTRY:
                return waypoints.get(0 + 1).getPosition();
            case LIGHT_AT_EXIT:
                return waypoints.get(waypoints.size() - 1 - 1).getPosition();
        }
        return null;
    }

    public boolean startsInCircle(int radius, double middleLat, double middleLon) {
        return getWaypoint(0).containedInCircle(radius, middleLat, middleLon);
    }

    public Integer determineRouteOrientationAndFilterRelevantNodes(OSMWay osmWay, Integer startingNodeIndex, Integer finishingNodeIndex) {
        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> tangentialNodes = determineTangentialNodes(osmWay);
        if (startingNodeIndex == null) {
            //return determineStartingNodeRefAndFilterRelevantNodes(tangentialNodes);
            return filterRelevantNodes(startingNodeIndex == null ? getWaypointCount() - 1 : startingNodeIndex, tangentialNodes);
        }
        else if (tangentialNodes == null) {
            return filterRelevantNodes(startingNodeIndex, finishingNodeIndex == null ? 0 : finishingNodeIndex);
        }
        else {
            return filterRelevantNodes(startingNodeIndex, tangentialNodes);
        }
    }

    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> determineTangentialNodes(OSMWay osmWay) {
        if (osmWay == null) {
            return null;
        }
        Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> tangentialNodesOursAndNext = Pair.with(null, null);
        for (int i = 0; i < osmWay.getWaypointCount(); ++i) {
            for (int j = 0; j < getWaypointCount(); ++j) {
                if (osmWay.getWaypoint(i).getOsmNodeRef().equals(getWaypoint(j).getOsmNodeRef())) {
                    if (tangentialNodesOursAndNext.getValue0() == null) {
                        tangentialNodesOursAndNext = tangentialNodesOursAndNext.setAt0(Pair.with(j, i));
                    }
                    else if (tangentialNodesOursAndNext.getValue1() == null) {
                        tangentialNodesOursAndNext = tangentialNodesOursAndNext.setAt1(Pair.with(j, i));
                    }
                    else {
                        return tangentialNodesOursAndNext;
                    }
                }
            }
        }
        return tangentialNodesOursAndNext;
    }

    private Integer determineStartingNodeRefAndFilterRelevantNodes(Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> tangentialNodes) {
        final int startFirstCount = wayCountBetween(0, tangentialNodes.getValue0().getValue0());
        final int endFirstCount = wayCountBetween(getWaypointCount() - 1, tangentialNodes.getValue0().getValue0());
        final int startSecondCount = tangentialNodes.getValue1() != null ? wayCountBetween(0, tangentialNodes.getValue1().getValue0()) : Integer.MAX_VALUE;
        final int endSecondCount = tangentialNodes.getValue1() != null ? wayCountBetween(getWaypointCount() - 1, tangentialNodes.getValue1().getValue0()) : Integer.MAX_VALUE;
        if ((startFirstCount < endFirstCount && startFirstCount < endSecondCount) ||
                startSecondCount < endFirstCount && startSecondCount < endSecondCount) {
            return filterRelevantNodes(0, tangentialNodes);
        }
        else {
            return filterRelevantNodes(getWaypointCount() - 1, tangentialNodes);
        }
    }

    private Integer filterRelevantNodes(Integer startingNodeIndex, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> tangentialNodes) {
        if (tangentialNodes.getValue1() != null &&
                wayCountBetween(startingNodeIndex, tangentialNodes.getValue1().getValue0()) <
                        wayCountBetween(startingNodeIndex, tangentialNodes.getValue0().getValue0())) {
            return filterRelevantNodes(startingNodeIndex, tangentialNodes.getValue1());
        }
        else {
            return filterRelevantNodes(startingNodeIndex, tangentialNodes.getValue0());
        }
    }

    private Integer filterRelevantNodes(int index1, Pair<Integer, Integer> index2pair) {
        filterRelevantNodes(index1, index2pair.getValue0());
        return index2pair.getValue1();
    }

    private Integer filterRelevantNodes(Integer startingNodeIndex, Integer finishingNodeIndex) {
        if (startingNodeIndex > finishingNodeIndex) {
            int tmp = startingNodeIndex;
            startingNodeIndex = finishingNodeIndex;
            finishingNodeIndex = tmp;
            routeOrientation = RouteOrientation.BACK;
        }
        final List<OSMWaypoint> newWaypoints = new ArrayList<>();
        for (int i = startingNodeIndex; i <= finishingNodeIndex; ++i) {
            newWaypoints.add(getWaypoint(i));
        }
        waypoints = newWaypoints;
        return null;
    }

    private int wayCountBetween(int index1, int index2) {
        return Math.abs(index1 - index2);
    }

    public Integer indexOf(final String osmNodeRef) {
        for (int i = 0; i < getWaypointCount(); ++i) {
            if (waypoints.get(i).getOsmNodeRef().equals(osmNodeRef)) {
                return i;
            }
        }
        return null;
    }

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
