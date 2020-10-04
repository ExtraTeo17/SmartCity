package routing;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.elements.OSMLight;
import osmproxy.elements.OSMWay;
import osmproxy.elements.OSMWay.RouteOrientation;
import osmproxy.elements.OSMWaypoint;
import routing.abstractions.IRouteGenerator;
import routing.abstractions.IRouteTransformer;
import routing.core.IGeoPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// TODO: Add fields to this class and make it some kind of service (not static)
final class Router implements
        IRouteGenerator,
        IRouteTransformer {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    private final IMapAccessManager mapAccessManager;
    private final NodesContainer nodesContainer;

    @Inject
    public Router(IMapAccessManager mapAccessManager, NodesContainer nodesContainer) {
        this.mapAccessManager = mapAccessManager;
        this.nodesContainer = nodesContainer;
    }

    @Override
    public List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = findRoute(pointA, pointB, false);
        var wayIds = osmWayIdsAndPointList.getValue0();
        if (wayIds == null || wayIds.isEmpty()) {
            logger.warn("Generating routeInfo failed because of empty wayIds.");
            return new ArrayList<>();
        }

        List<OSMLight> lightInfo = mapAccessManager.getOsmLights(wayIds);
        var managers = getManagersNodesForLights(lightInfo);
        var points = osmWayIdsAndPointList.getValue1();

        return getRouteWithAdditionalNodes(points, managers);
    }

    // TODO: Merge with function for cars if testing proves they are identical
    @Override
    public List<RouteNode> generateRouteForPedestrians(IGeoPosition pointA, IGeoPosition pointB) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = Router.findRoute(pointA, pointB, true);
        final List<OSMLight> lightInfo = mapAccessManager.getOsmLights(osmWayIdsAndPointList.getValue0());
        List<RouteNode> managers = getManagersNodesForLights(lightInfo);
        return getRouteWithAdditionalNodes(osmWayIdsAndPointList.getValue1(), managers);
    }

    // TODO: Improve routing to consider random OSM nodes as start/end points instead of random lat/lng
    // TODO: Always: either starting == null or finishing == null
    @Override
    @Beta
    public List<RouteNode> generateRouteForPedestrians(IGeoPosition pointA, IGeoPosition pointB,
                                                       String startingOsmNodeRef, String finishingOsmNodeRef) {
        var osmWayIdsAndPointList = findRoute(pointA, pointB, true);
        var osmWayIds = osmWayIdsAndPointList.getValue0();
        var routeInfoOpt = mapAccessManager.getRouteInfo(osmWayIds);
        if (routeInfoOpt.isEmpty()) {
            logger.warn("Generating route for pedestrians failed because of empty routeInfo");
            return new ArrayList<>();
        }

        var routeInfo = routeInfoOpt.get();
        routeInfo.determineRouteOrientationsAndFilterRelevantNodes(startingOsmNodeRef, finishingOsmNodeRef);

        return createRouteNodeList(routeInfo);
    }

    private List<RouteNode> createRouteNodeList(RouteInfo routeInfo) {
        List<RouteNode> routeNodes = new ArrayList<>();
        for (var route : routeInfo) {
            int waypointCount = route.getWaypointCount();
            if (route.getRouteOrientation() == RouteOrientation.FRONT) {
                for (int j = 0; j < waypointCount; ++j) {
                    var node = getNode(route.getWaypoint(j), routeInfo);
                    routeNodes.add(node);
                }
            }
            else {
                for (int j = waypointCount - 1; j >= 0; --j) {
                    var node = getNode(route.getWaypoint(j), routeInfo);
                    routeNodes.add(node);
                }
            }
        }

        return routeNodes;
    }

    private RouteNode getNode(OSMWaypoint waypoint, RouteInfo routeInfo) {
        long nodeRefId = Long.parseLong(waypoint.getOsmNodeRef());
        // TODO: Is it needed?
        if (routeInfo.remove(nodeRefId)) {
            return nodesContainer.getNode(nodeRefId);
        }

        return new RouteNode(waypoint.getLat(), waypoint.getLng());
    }

    @Override
    public List<RouteNode> generateRouteInfoForBuses(List<OSMWay> route, List<? extends IGeoPosition> stationsPositions) {
        var busRouteData = generateBusRoute(route);
        List<OSMLight> lightsOnRoute = mapAccessManager.getOsmLights(busRouteData.waysIds);
        List<RouteNode> managers = getManagersNodesForLights(lightsOnRoute);
        List<RouteNode> stationNodes = stationsPositions.stream()
                .map(s -> new RouteNode(s.getLat(), s.getLng()))
                .collect(Collectors.toList());
        managers.addAll(stationNodes);
        return getRouteWithAdditionalNodes(busRouteData.route, managers);
    }

    // TODO: In some cases distance is 0 -> dx|dy is NaN -> same nodes?
    @SuppressWarnings("FeatureEnvy")
    @Override
    public List<RouteNode> uniformRoute(List<RouteNode> route) {
        List<RouteNode> newRoute = new ArrayList<>();
        for (int i = 0; i < route.size() - 1; ++i) {
            RouteNode routeA = route.get(i);
            RouteNode routeB = route.get(i + 1);

            double x = routeB.getLng() - routeA.getLng();
            double y = routeB.getLat() - routeA.getLat();

            double distance = RoutingConstants.METERS_PER_DEGREE * Math.sqrt(x * x + y * y);
            if (distance == 0) {
                continue;
            }

            double dx = x / distance;
            double dy = y / distance;

            double lon = routeA.getLng();
            double lat = routeA.getLat();
            newRoute.add(routeA);
            for (int p = RoutingConstants.STEP_SIZE_METERS; p < distance; p += RoutingConstants.STEP_SIZE_METERS) {
                lon = lon + RoutingConstants.STEP_SIZE_METERS * dx;
                lat = lat + RoutingConstants.STEP_SIZE_METERS * dy;
                newRoute.add(new RouteNode(lat, lon));
            }
        }

        if (route.size() > 0) {
            newRoute.add(route.get(route.size() - 1));
        }

        return newRoute;
    }

    /////////////////////////////////////////////////////////////
    //  HELPERS - Most are abominable :(
    /////////////////////////////////////////////////////////////


    private static Pair<List<Long>, List<RouteNode>> findRoute(IGeoPosition pointA, IGeoPosition pointB, boolean onFoot) {
        return osmproxy.HighwayAccessor.getOsmWayIdsAndPointList(pointA.getLat(), pointA.getLng(), pointB.getLat(),
                pointB.getLng(), onFoot);
    }

    private List<RouteNode> getManagersNodesForLights(List<OSMLight> lights) {
        List<RouteNode> nodes = new ArrayList<>();
        long lastMangerId = -1;
        for (OSMLight light : lights) {
            LightManagerNode nodeToAdd = nodesContainer.getNode(light.getAdherentWayId(), light.getId());
            if (nodeToAdd != null) {
                var nodeManagerId = nodeToAdd.getLightManagerId();
                if (nodeManagerId != lastMangerId) {
                    nodes.add(nodeToAdd);
                    lastMangerId = nodeManagerId;
                }
            }
        }
        return nodes;
    }

    private static List<RouteNode> getRouteWithAdditionalNodes(List<RouteNode> route, List<RouteNode> additionalNodes) {
        for (RouteNode node : additionalNodes) {
            int index = findPositionOfElementOnRoute(route, node);
            route.add(index, node);
        }
        return route;
    }

    @SuppressWarnings("FeatureEnvy")
    private static int findPositionOfElementOnRoute(List<RouteNode> route, RouteNode manager) {
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
            return minIndex + 1;
        }

        var nodeA = route.get(minIndex - 1);
        var nodeB = route.get(minIndex);
        double distAToManager = nodeA.distance(manager);
        double distAB = nodeA.distance(nodeB);
        if (distAToManager < distAB) {
            return minIndex;
        }

        return minIndex + 1;
    }

    private static BusRouteData generateBusRoute(List<OSMWay> route) {
        List<Long> osmWaysIds = new ArrayList<>();
        List<RouteNode> routeNodes = new ArrayList<>();
        for (OSMWay way : route) {
            osmWaysIds.add(way.getId());
            var nodes = way.getWaypoints().stream()
                    .map(RouteNode::new).collect(Collectors.toList());
            routeNodes.addAll(nodes);
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


}
