package routing;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;
import org.javatuples.Pair;
import osmproxy.MapAccessManager;
import osmproxy.elements.OSMLight;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import osmproxy.elements.OSMWay.RelationOrientation;
import osmproxy.elements.OSMWay.RouteOrientation;
import osmproxy.elements.OSMWaypoint;
import routing.core.IGeoPosition;
import smartcity.MasterAgent;
import utilities.NumericHelper;

import java.util.ArrayList;
import java.util.List;

// TODO: Add fields to this class and make it some kind of service (not static)
public final class Router {
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

    public static List<RouteNode> generateRouteInfoForBuses(List<OSMWay> router, List<Long> osmStationIds) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = findBusRoute(router);
        List<OSMLight> lightsOnRoute = MapAccessManager.sendFullTrafficSignalQuery(osmWayIdsAndPointList.getValue0());
        List<RouteNode> managers = getManagersForLights(lightsOnRoute);
        List<RouteNode> stationNodes = getAgentStationsForRoute(getOSMNodesForStations(osmStationIds));
        managers.addAll(stationNodes);
        return getRouteWithAdditionalNodes(osmWayIdsAndPointList.getValue1(), managers);
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

    public static List<RouteNode> uniformRoute(List<RouteNode> route) {
        List<RouteNode> newRoute = new ArrayList<>();

        for (int i = 0; i < route.size() - 1; i++) {
            RouteNode routeA = route.get(i);
            RouteNode routeB = route.get(i + 1);

            double x = routeB.getLng() - routeA.getLng();
            double y = routeB.getLat() - routeA.getLat();

            double distance = NumericHelper.METERS_PER_DEGREE * Math.sqrt(x * x + y * y);

            double dx = x / distance;
            double dy = y / distance;

            double lon = routeA.getLng();
            double lat = routeA.getLat();
            newRoute.add(routeA);
            for (int p = 1; p < distance; p++) {
                lon = lon + dx;
                lat = lat + dy;
                newRoute.add(new RouteNode(lat, lon));
            }
        }

        newRoute.add(route.get(route.size() - 1));

        return newRoute;
    }

    private static List<OSMNode> getOSMNodesForStations(List<Long> stationsIDs) {
        List<OSMNode> listOsmNodes = new ArrayList<>();
        for (long station : stationsIDs) {
            listOsmNodes.add(MasterAgent.osmIdToStationOSMNode.get(station));
        }
        return listOsmNodes;
    }

    private static Pair<List<Long>, List<RouteNode>> findBusRoute(List<OSMWay> router) {
        List<Long> osmWayIds_list = new ArrayList<>();
        List<RouteNode> RouteNodes_list = new ArrayList<>();
        for (OSMWay el : router) {
            osmWayIds_list.add(el.getId());
            addRouteNodesBasedOnOrientation(el, RouteNodes_list);
        }
        return new Pair<>(osmWayIds_list, RouteNodes_list);
    }

    private static void addRouteNodesBasedOnOrientation(OSMWay osmWay, List<RouteNode> routeNodes) {
        var orientation = osmWay.getRelationOrientation();
        var waypoints = osmWay.getWaypoints();
        if (orientation == RelationOrientation.FRONT) {
            for (var point : waypoints) {
                routeNodes.add(new RouteNode(point));
            }
            return;
        }
        else if (orientation == RelationOrientation.BACK) {
            // Warn: waypoints list is not changed here.
            for (var point : Lists.reverse(waypoints)) {
                routeNodes.add(new RouteNode(point));
            }
            return;
        }

        throw new UnsupportedOperationException("Orientation " + orientation.toString() + " is not supported");
    }

    private static List<RouteNode> getAgentStationsForRoute(List<OSMNode> stations) {
        List<RouteNode> managers = new ArrayList<>();
        for (OSMNode station : stations) {
            addStationNodeToList(managers, station);
        }
        return managers;
    }

    private static void addStationNodeToList(List<RouteNode> stations, OSMNode station) {
        RouteNode nodeToAdd = MasterAgent.osmStationIdToStationNode.get(station.getId());
        stations.add(nodeToAdd);
    }
}
