package routing;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.MapAccessManager;
import osmproxy.elements.OSMLight;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import osmproxy.elements.OSMWay.RelationOrientation;
import osmproxy.elements.OSMWay.RouteOrientation;
import osmproxy.elements.OSMWaypoint;
import routing.core.IGeoPosition;
import smartcity.MasterAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// TODO: Add fields to this class and make it some kind of service (not static)
public final class Router {
    public static final int STEP_SIZE_METERS = 1;
    public static final int M_MILLISECONDS_TO_KM_HOUR = 3600;
    public static final int STEP_CONSTANT = STEP_SIZE_METERS * M_MILLISECONDS_TO_KM_HOUR;

    public static final double EARTH_RADIUS_METERS = 6_378_137;
    public static final double METERS_PER_DEGREE = EARTH_RADIUS_METERS * Math.PI / 180.0;
    public static final double DEGREES_PER_METER = 1 / METERS_PER_DEGREE;

    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    public static List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = Router.findRoute(pointA, pointB, false);
        final List<OSMLight> lightInfo = MapAccessManager.sendFullTrafficSignalQuery(osmWayIdsAndPointList.getValue0());
        List<RouteNode> managers = Router.getManagersForLights(lightInfo);
        return Router.getRouteWithAdditionalNodes(osmWayIdsAndPointList.getValue1(), managers);
    }

    // TODO: Merge with function for cars if testing proves they are identical
    @Deprecated
    public static List<RouteNode> generateRouteInfoForPedestrians(IGeoPosition pointA, IGeoPosition pointB) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = Router.findRoute(pointA, pointB, true);
        final List<OSMLight> lightInfo = MapAccessManager.sendFullTrafficSignalQuery(osmWayIdsAndPointList.getValue0());
        List<RouteNode> managers = Router.getManagersForLights(lightInfo);
        return getRouteWithAdditionalNodes(osmWayIdsAndPointList.getValue1(), managers);
    }

    // TODO: Improve routing to consider random OSM nodes as start/end points instead of random lat/lng
    // TODO: Always: either starting == null or finishing == null
    @Beta
    public static List<RouteNode> generateRouteInfoForPedestrians(IGeoPosition pointA, IGeoPosition pointB,
                                                                  String startingOsmNodeRef, String finishingOsmNodeRef) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = Router.findRoute(pointA, pointB, true);
        final RouteInfo routeInfo = MapAccessManager.sendMultipleWayAndItsNodesQuery(osmWayIdsAndPointList.getValue0());
        routeInfo.determineRouteOrientationsAndFilterRelevantNodes(startingOsmNodeRef, finishingOsmNodeRef);

        return createRouteNodeList(routeInfo);
    }

    public static List<RouteNode> generateRouteInfoForBuses(List<OSMWay> route, List<? extends OSMNode> osmStations) {
        var busRouteData = generateBusRoute(route);
        List<OSMLight> lightsOnRoute = MapAccessManager.sendFullTrafficSignalQuery(busRouteData.waysIds);
        List<RouteNode> managers = getManagersForLights(lightsOnRoute);
        List<RouteNode> stationNodes = getAgentStationsForRoute(osmStations);
        managers.addAll(stationNodes);
        return getRouteWithAdditionalNodes(busRouteData.route, managers);
    }

    private static List<RouteNode> createRouteNodeList(RouteInfo routeInfo) {
        List<RouteNode> routeNodes = new ArrayList<>();
        for (var route : routeInfo) {
            int waypointCount = route.getWaypointCount();
            if (route.getRouteOrientation() == RouteOrientation.FRONT) {
                for (int j = 0; j < waypointCount; ++j) {
                    routeNodes.add(getRoute(route.getWaypoint(j), routeInfo));
                }
            }
            else {
                for (int j = waypointCount - 1; j >= 0; --j) {
                    routeNodes.add(getRoute(route.getWaypoint(j), routeInfo));
                }
            }
        }

        return routeNodes;
    }

    private static RouteNode getRoute(OSMWaypoint waypoint, RouteInfo routeInfo) {
        String nodeRef = waypoint.getOsmNodeRef();
        if (routeInfo.remove(nodeRef)) {
            return MasterAgent.crossingOsmIdToLightManagerNode.get(Long.parseLong(nodeRef));
        }

        return new RouteNode(waypoint.getLat(), waypoint.getLng());
    }

    /////////////////////////////////////////////////////////////
    //  HELPERS
    /////////////////////////////////////////////////////////////

    private static Pair<List<Long>, List<RouteNode>> findRoute(IGeoPosition pointA, IGeoPosition pointB, boolean onFoot) {
        return osmproxy.HighwayAccessor.getOsmWayIdsAndPointList(pointA.getLat(), pointA.getLng(), pointB.getLat(),
                pointB.getLng(), onFoot);
    }

    private static List<RouteNode> getManagersForLights(List<OSMLight> lights) {
        List<RouteNode> managers = new ArrayList<>();
        for (OSMLight light : lights) {
            addLightManagerNodeToManagersList(managers, light);
        }
        return managers;
    }

    private static void addLightManagerNodeToManagersList(List<RouteNode> managers, OSMLight light) {
        Pair<Long, Long> osmWayIdOsmLightId = Pair.with(light.getAdherentWayId(), light.getId());
        RouteNode nodeToAdd = MasterAgent.wayIdLightIdToLightManagerNode.get(osmWayIdOsmLightId);
        if (nodeToAdd != null && !lastManagerIdEqualTo(managers, nodeToAdd)) {
            managers.add(nodeToAdd);
        }
    }

    private static boolean lastManagerIdEqualTo(List<RouteNode> managers, RouteNode nodeToAdd) {
        if (managers.size() == 0) {
            return false;
        }
        LightManagerNode lastNodeOnList = (LightManagerNode) managers.get(managers.size() - 1);
        return lastNodeOnList.getLightManagerId() == ((LightManagerNode) nodeToAdd).getLightManagerId();
    }


    private static List<RouteNode> getRouteWithAdditionalNodes(List<RouteNode> route, List<RouteNode> more_nodes) {
        for (RouteNode node : more_nodes) {
            findPositionOfElementOnRoute(route, node);
        }
        return route;
    }


    private static void findPositionOfElementOnRoute(List<RouteNode> route, RouteNode manager) {
        int minIndex = -1;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < route.size(); ++i) {
            double distance = route.get(i).distance(manager);
            if (distance < minDistance) {
                minDistance = distance;
                minIndex = i;
            }
        }
        if (minIndex <= 0) {
            route.add(minIndex + 1, manager);
            return;
        }
        double distMgrToMinPrev = route.get(minIndex - 1).distance(manager);
        double distMinToMinPrev = route.get(minIndex - 1).distance(route.get(minIndex));
        if (distMgrToMinPrev < distMinToMinPrev) {
            route.add(minIndex, manager);
        }
        else {
            route.add(minIndex + 1, manager);
        }
    }

    // TODO: In some cases distance is 0 -> dx/dy is NaN -> same nodes?
    public static List<RouteNode> uniformRoute(List<RouteNode> route) {
        List<RouteNode> newRoute = new ArrayList<>();
        for (int i = 0; i < route.size() - 1; i++) {
            RouteNode routeA = route.get(i);
            RouteNode routeB = route.get(i + 1);

            double x = routeB.getLng() - routeA.getLng();
            double y = routeB.getLat() - routeA.getLat();

            double distance = METERS_PER_DEGREE * Math.sqrt(x * x + y * y);

            double dx = x / distance;
            double dy = y / distance;

            double lon = routeA.getLng();
            double lat = routeA.getLat();
            newRoute.add(routeA);
            for (int p = STEP_SIZE_METERS; p < distance; p += STEP_SIZE_METERS) {
                lon = lon + STEP_SIZE_METERS * dx;
                lat = lat + STEP_SIZE_METERS * dy;
                newRoute.add(new RouteNode(lat, lon));
            }
        }
        newRoute.add(route.get(route.size() - 1));

        return newRoute;
    }

    private static BusRouteData generateBusRoute(List<OSMWay> route) {
        List<Long> osmWaysIds = new ArrayList<>();
        List<RouteNode> routeNodes = new ArrayList<>();
        for (OSMWay way : route) {
            osmWaysIds.add(way.getId());
            routeNodes.addAll(getNodesBasedOnOrientation(way));
        }
        return new BusRouteData(osmWaysIds, routeNodes);
    }

    private static class BusRouteData {
        private final List<Long> waysIds;
        private final List<RouteNode> route;

        private BusRouteData(List<Long> waysIds, List<RouteNode> route) {
            this.waysIds = waysIds;
            this.route = route;
        }
    }

    private static List<RouteNode> getNodesBasedOnOrientation(OSMWay osmWay) {
        var orientation = osmWay.getRelationOrientation();
        var waypoints = osmWay.getWaypoints();
        if (orientation == RelationOrientation.FRONT) {
            return waypoints.stream().map(RouteNode::new).collect(Collectors.toList());
        }
        else if (orientation == RelationOrientation.BACK) {
            // Waypoints list is not changed here.
            return Lists.reverse(waypoints).stream().map(RouteNode::new).collect(Collectors.toList());
        }

        throw new UnsupportedOperationException("Orientation " + orientation.toString() + " is not supported");
    }

    private static List<RouteNode> getAgentStationsForRoute(List<? extends OSMNode> stations) {
        List<RouteNode> managers = new ArrayList<>();
        for (OSMNode station : stations) {
            RouteNode nodeToAdd = MasterAgent.osmStationIdToStationNode.get(station.getId());
            managers.add(nodeToAdd);
        }
        return managers;
    }
}
