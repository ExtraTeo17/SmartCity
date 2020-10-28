package routing;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.abstractions.ICacheWrapper;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.elements.OSMLight;
import osmproxy.elements.OSMWay;
import osmproxy.elements.OSMWay.RouteOrientation;
import osmproxy.elements.OSMWaypoint;
import routing.abstractions.INodesContainer;
import routing.abstractions.IRouteGenerator;
import routing.core.IGeoPosition;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class Router implements
        IRouteGenerator {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    private final IMapAccessManager mapAccessManager;
    private final INodesContainer nodesContainer;
    private final ICacheWrapper cacheWrapper;

    @Inject
    public Router(IMapAccessManager mapAccessManager,
                  INodesContainer nodesContainer,
                  ICacheWrapper cacheWrapper) {
        this.mapAccessManager = mapAccessManager;
        this.nodesContainer = nodesContainer;
        this.cacheWrapper = cacheWrapper;
    }

    @Override
    public List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB, boolean bewareOfJammedRoutes) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = findRoute(pointA, pointB, false, bewareOfJammedRoutes);
        var wayIds = osmWayIdsAndPointList.getValue0();
        if (wayIds == null || wayIds.isEmpty()) {
            logger.warn("Generating routeInfo failed because of empty wayIds.");
            return new ArrayList<>();
        }

        List<OSMLight> lightInfo = mapAccessManager.getOsmLights(wayIds);
        var managersNodes = getManagersNodesForLights(lightInfo);
        var points = osmWayIdsAndPointList.getValue1();

        return getRouteWithAdditionalNodes(points, managersNodes);
    }

    // TODO: Merge with function for cars if testing proves they are identical
    @Override
    public List<RouteNode> generateRouteForPedestrians(IGeoPosition pointA, IGeoPosition pointB) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = Router.findRoute(pointA, pointB, true, false);
        List<OSMLight> lightInfo = mapAccessManager.getOsmLights(osmWayIdsAndPointList.getValue0());
        List<RouteNode> managersNodes = getManagersNodesForLights(lightInfo);
        return getRouteWithAdditionalNodes(osmWayIdsAndPointList.getValue1(), managersNodes);
    }

    // TODO: Improve routing to consider random OSM nodes as start/end points instead of random lat/lng
    // TODO: Always: either starting == null or finishing == null
    @Override
    @Beta
    public List<RouteNode> generateRouteForPedestrians(IGeoPosition pointA, IGeoPosition pointB,
                                                       String startingOsmNodeRef, String finishingOsmNodeRef) {
        var osmWayIdsAndPointList = findRoute(pointA, pointB, true, false);
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
            return nodesContainer.getLightManagerNode(nodeRefId);
        }

        return new RouteNode(waypoint.getLat(), waypoint.getLng());
    }

    @Override
    public List<RouteNode> generateRouteInfoForBuses(List<OSMWay> route,
                                                     List<StationNode> stationNodes) {
        var data = cacheWrapper.getBusRoute(route, stationNodes);
        if (data.size() > 0) {
            if (updateCacheDataAgentId(data, stationNodes)) {
                return data;
            }
        }

        var busRouteData = generateBusRoute(route);
        List<OSMLight> lightsOnRoute = mapAccessManager.getOsmLights(busRouteData.waysIds);
        List<RouteNode> managersNodes = getManagersNodesForLights(lightsOnRoute);
        managersNodes.addAll(stationNodes);

        data = getRouteWithAdditionalNodes(busRouteData.route, managersNodes);
        cacheWrapper.cacheData(route, stationNodes, data);

        return data;
    }

    /////////////////////////////////////////////////////////////
    //  HELPERS - Most are abominable :(
    /////////////////////////////////////////////////////////////

    private boolean updateCacheDataAgentId(List<RouteNode> data, List<StationNode> stationNodes) {
        for (RouteNode node : data) {
            if (node instanceof StationNode) {
                var st = (StationNode) node;
                var newStation = stationNodes.stream().filter(f -> f.getOsmId() == st.getOsmId()).findFirst();
                if (newStation.isPresent()) {
                    st.setAgentId(newStation.get().getAgentId());
                }
                else {
                    logger.warn("Skipping cache because station not found on the route");
                    return false;
                }
            }
        }

        return true;
    }


    private static Pair<List<Long>, List<RouteNode>> findRoute(IGeoPosition pointA, IGeoPosition pointB,
                                                               boolean onFoot, boolean bewareOfJammedRoutes) {
        return osmproxy.HighwayAccessor.getOsmWayIdsAndPointList(pointA.getLat(), pointA.getLng(), pointB.getLat(),
                pointB.getLng(), onFoot, bewareOfJammedRoutes);
    }

    private List<RouteNode> getManagersNodesForLights(List<OSMLight> lights) {
        List<RouteNode> nodes = new ArrayList<>();
        long lastMangerId = -1;
        for (OSMLight light : lights) {
            var nodeToAdd = nodesContainer.getLightManagerNode(light.getAdherentWayId(), light.getId());
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

    private static <T extends List<RouteNode>> T getRouteWithAdditionalNodes(T route, List<RouteNode> additionalNodes) {
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
        var osmWaysIds = new ArrayList<Long>();
        var routeNodes = new ArrayList<RouteNode>();
        for (OSMWay way : route) {
            osmWaysIds.add(way.getId());
            var nodes = way.getWaypoints().stream()
                    .map(RouteNode::new).collect(Collectors.toList());
            routeNodes.addAll(nodes);
        }
        return new BusRouteData(osmWaysIds, routeNodes);
    }

    private static class BusRouteData {
        private final ArrayList<Long> waysIds;
        private final ArrayList<RouteNode> route;

        private BusRouteData(ArrayList<Long> waysIds, ArrayList<RouteNode> route) {
            this.waysIds = waysIds;
            this.route = route;
        }
    }


}
