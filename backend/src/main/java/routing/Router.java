package routing;

import com.google.common.annotations.Beta;
import org.javatuples.Pair;
import org.jxmapviewer.viewer.GeoPosition;
import osmproxy.MapAccessManager;
import osmproxy.elements.OSMLight;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import osmproxy.elements.OSMWay.RelationOrientation;
import osmproxy.elements.OSMWay.RouteOrientation;
import osmproxy.elements.OSMWaypoint;
import smartcity.MasterAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static utilities.CalculationHelper.getEuclideanDistance;

// TODO: Add fields to this class and make it some kind of service (not static)
public final class Router {
    public static List<RouteNode> generateRouteInfo(GeoPosition pointA, GeoPosition pointB) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = Router.findRoute(pointA, pointB, false);
        final List<OSMLight> lightInfo = MapAccessManager.sendFullTrafficSignalQuery(osmWayIdsAndPointList.getValue0());
        List<RouteNode> managers = Router.getManagersForLights(lightInfo);
        return Router.getRouteWithAdditionalNodes(osmWayIdsAndPointList.getValue1(), managers);
    }

    // TODO: Merge with function for cars if testing proves they are identical
    @Deprecated
    public static List<RouteNode> generateRouteInfoForPedestrians(GeoPosition pointA, GeoPosition pointB) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = Router.findRoute(pointA, pointB, true);
        final List<OSMLight> lightInfo = MapAccessManager.sendFullTrafficSignalQuery(osmWayIdsAndPointList.getValue0());
        List<RouteNode> managers = Router.getManagersForLights(lightInfo);
        return getRouteWithAdditionalNodes(osmWayIdsAndPointList.getValue1(), managers);
    }

    // TODO: Improve routing to consider random OSM nodes as start/end points instead of random lat/lng
    // TODO: Always: either starting == null or finishing == null
    @Beta
    public static List<RouteNode> generateRouteInfoForPedestrians(GeoPosition pointA, GeoPosition pointB,
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

        return new RouteNode(waypoint.getPosition());
    }

    /////////////////////////////////////////////////////////////
    //  HELPERS
    /////////////////////////////////////////////////////////////

    private static Pair<List<Long>, List<RouteNode>> findRoute(GeoPosition pointA, GeoPosition pointB, boolean onFoot) {
        return osmproxy.HighwayAccessor.getOsmWayIdsAndPointList(pointA.getLatitude(), pointA.getLongitude(), pointB.getLatitude(),
                pointB.getLongitude(), onFoot);
    }

    private static List<RouteNode> getManagersForLights(List<OSMLight> lights) {
        List<RouteNode> managers = new ArrayList<>();
        for (OSMLight light : lights) {
            addLightManagerNodeToManagersList(managers, light);
        }
        return managers;
    }

    private static void addLightManagerNodeToManagersList(List<RouteNode> managers, OSMLight light) {
        Pair<Long, Long> osmWayIdOsmLightId = Pair.with(light.getAdherentOsmWayId(), light.getId());
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
            double distance = getEuclideanDistance(route.get(i), manager);
            if (distance < minDistance) {
                minDistance = distance;
                minIndex = i;
            }
        }
        if (minIndex <= 0) {
            route.add(minIndex + 1, manager);
            return;
        }
        double distMgrToMinPrev = getEuclideanDistance(route.get(minIndex - 1), manager);
        double distMinToMinPrev = getEuclideanDistance(route.get(minIndex - 1), route.get(minIndex));
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
            double x = routeB.getLongitude() - routeA.getLongitude();
            double y = routeB.getLatitude() - routeA.getLatitude();

            // TODO: What?
            double xInMeters = x * 111111;
            double yInMeters = y * 111111;

            double distance = Math.sqrt(xInMeters * xInMeters + yInMeters * yInMeters);

            double dx = x / distance;
            double dy = y / distance;

            newRoute.add(routeA);

            double lon = routeA.getLongitude();
            double lat = routeA.getLatitude();

            for (int p = 1; p < distance; p++) {
                lon = lon + dx;
                lat = lat + dy;
                RouteNode node = new RouteNode(lat, lon);
                newRoute.add(node);
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

    private static void addRouteNodesBasedOnOrientation(OSMWay el, List<RouteNode> routeNodes_list) {
        var orientation = el.getRelationOrientation();
        var waypoints = el.getWaypoints();
        if (orientation == RelationOrientation.FRONT) {
            addRouteNodesToList(waypoints, routeNodes_list);
            return;
        }
        else if (orientation == RelationOrientation.BACK) {
            addRouteNodesToList(reverse(waypoints), routeNodes_list);
            return;
        }

        throw new UnsupportedOperationException("Orientation " + orientation.toString() + " is not supported");
    }

    private static List<OSMWaypoint> reverse(List<OSMWaypoint> waypoints) {
        List<OSMWaypoint> waypointsCopy = new ArrayList<>(waypoints);
        Collections.reverse(waypointsCopy);
        return waypointsCopy;
    }

    private static void addRouteNodesToList(List<OSMWaypoint> waypoints, List<RouteNode> routeNodes_list) {
        for (OSMWaypoint point : waypoints) {
            routeNodes_list.add(new RouteNode(point.getLat(), point.getLon()));
        }
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
