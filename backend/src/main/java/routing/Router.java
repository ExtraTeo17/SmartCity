package routing;

import com.google.inject.Inject;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.abstractions.ICacheWrapper;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.elements.OSMLight;
import osmproxy.elements.OSMWay;
import osmproxy.elements.OSMWaypoint;
import osmproxy.elements.data.RouteOrientation;
import osmproxy.routes.abstractions.IHighwayAccessor;
import routing.abstractions.INodesContainer;
import routing.abstractions.IRouteGenerator;
import routing.abstractions.IRouteTransformer;
import routing.core.IGeoPosition;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

final class Router implements
        IRouteGenerator {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    private final IMapAccessManager mapAccessManager;
    private final INodesContainer nodesContainer;
    private final ICacheWrapper cacheWrapper;
    private final IRouteTransformer routeTransformer;
    private final IHighwayAccessor highwayAccessor;

    @Inject
    public Router(IMapAccessManager mapAccessManager,
                  INodesContainer nodesContainer,
                  ICacheWrapper cacheWrapper,
                  IRouteTransformer routeTransformer,
                  IHighwayAccessor highwayAccessor) {
        this.mapAccessManager = mapAccessManager;
        this.nodesContainer = nodesContainer;
        this.cacheWrapper = cacheWrapper;
        this.routeTransformer = routeTransformer;
        this.highwayAccessor = highwayAccessor;
    }

    // TODO: now with new route generation there is sometimes "failed to get adjacent osmwayId" error, check it out
    @Override

    public List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB,
                                             String typeOfVehicle,
                                             StationNode startStation,
                                             StationNode endStation,
                                             boolean bewareOfJammedEdge) {
        boolean isNotPedestrian = !typeOfVehicle.equals("foot");
        var osmWayIdsAndEdgeList = findRoute(pointA, pointB, typeOfVehicle, bewareOfJammedEdge);
        var osmWayIds = osmWayIdsAndEdgeList.getValue0();

        // TODO: refactor inside to throw exception if not car or pedestrian
        var routeInfoOpt = mapAccessManager.getRouteInfo(osmWayIds, isNotPedestrian);
        if (routeInfoOpt.isEmpty()) {
            logger.warn("Generating route failed because of empty routeInfo");
            return new ArrayList<>();
        }

        var routeInfo = routeInfoOpt.get();
        if (routeInfo.hasNoWays()) {
            logger.warn("No ways on route");
            return new ArrayList<>();
        }

        var startingOsmNodeRef = routeInfo.getFirst().findClosestNodeRefTo(pointA);
        var finishingOsmNodeRef = routeInfo.getLast().findClosestNodeRefTo(pointB);

        try {
            routeInfo.determineRouteOrientationsAndFilterRelevantNodes(startingOsmNodeRef, finishingOsmNodeRef);
        } catch (NoSuchElementException e) {
            logger.info("GraphHopper API is not able to create route for provided points.");
            return new ArrayList<>();
        }
        var route = createRouteNodeList(routeInfo, isNotPedestrian, startStation, endStation,
                startingOsmNodeRef, finishingOsmNodeRef);
        return routeTransformer.uniformRoute(route, osmWayIdsAndEdgeList.getValue1());
    }

    private List<RouteNode> createRouteNodeList(RouteInfo routeInfo, boolean isNotPedestrian,
                                                StationNode startStation, StationNode endStation, String startingOsmNodeRef,
                                                String finishingOsmNodeRef) {
        List<RouteNode> routeNodes = new ArrayList<>();

        if (!isNotPedestrian && endStation != null && !startingOsmNodeRef.equals(String.valueOf(endStation.getOsmId()))) {
            routeNodes.add(new RouteNode(endStation));
        }

        for (var way : routeInfo) {
            int waypointCount = way.getWaypointCount();
            int lastLightManagerId = -1;

            boolean straight = way.getRouteOrientation() == RouteOrientation.FRONT;
            int startingIndex = straight ? 0 : waypointCount - 1;
            int lastIndex = straight ? waypointCount : -1;
            int increment = straight ? 1 : -1;
            for (int j = startingIndex; j != lastIndex; j += increment) {
                var nodeOpt = getNode(way, way.getWaypoint(j), routeInfo, isNotPedestrian);
                if (nodeOpt.isEmpty()) {
                    continue;
                }

                var node = nodeOpt.get();
                if (node instanceof LightManagerNode) {
                    int lightManagerId = ((LightManagerNode) node).getLightManagerId();
                    if (!(lightManagerId == lastLightManagerId)) {
                        routeNodes.add(node);
                        lastLightManagerId = lightManagerId;
                    }
                }
                else {
                    routeNodes.add(node);
                }

            }
        }

        if (!isNotPedestrian && startStation != null && !finishingOsmNodeRef.equals(String.valueOf(startStation.getOsmId()))) {
            routeNodes.add(new RouteNode(startStation));
        }

        return routeNodes;
    }

    private Optional<RouteNode> getNode(OSMWay way, OSMWaypoint waypoint, RouteInfo routeInfo, boolean isNotPedestrian) {
        long wayId = way.getId();
        long nodeRefId = Long.parseLong(waypoint.getOsmNodeRef());

        RouteNode result;
        if (routeInfo.remove(nodeRefId)) {
            result = isNotPedestrian ? nodesContainer.getLightManagerNode(wayId, nodeRefId) :
                    nodesContainer.getLightManagerNode(nodeRefId);
        }
        else {
            result = new RouteNode(waypoint.getLat(), waypoint.getLng(), false);
        }

        return Optional.ofNullable(result);
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
        ArrayList<RouteNode> data2 = (ArrayList<RouteNode>) routeTransformer.uniformRoute(data);
        cacheWrapper.cacheData(route, stationNodes, data2);

        return data2;
    }


    /////////////////////////////////////////////////////////////
    //  HELPERS
    /////////////////////////////////////////////////////////////

    private boolean updateCacheDataAgentId(List<RouteNode> data, List<StationNode> stationNodes) {
        for (RouteNode node : data) {
            if (node instanceof StationNode) {
                var st = (StationNode) node;
                var newStation = stationNodes.stream().filter(f -> f.getOsmId() == st.getOsmId()).findFirst();
                if (newStation.isEmpty()) {
                    logger.warn("Skipping cache because station not found on the route");
                    return false;
                }

                st.setAgentId(newStation.get().getAgentId());
            }
        }

        return true;
    }

    private Pair<List<Long>, List<Integer>> findRoute(IGeoPosition pointA,
                                                      IGeoPosition pointB,
                                                      String typeOfVehicle, boolean bewareOfJammedEdge) {
        return highwayAccessor.getOsmWayIdsAndEdgeList(pointA, pointB, typeOfVehicle, bewareOfJammedEdge);
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

    private static RouteData generateBusRoute(List<OSMWay> route) {
        var osmWaysIds = new ArrayList<Long>();
        var routeNodes = new ArrayList<RouteNode>();
        for (OSMWay way : route) {
            osmWaysIds.add(way.getId());
            var nodes = way.getWaypoints().stream()
                    .map(RouteNode::new).collect(Collectors.toList());
            routeNodes.addAll(nodes);
        }
        return new RouteData(osmWaysIds, routeNodes);
    }

    private static class RouteData {
        private final ArrayList<Long> waysIds;
        private final ArrayList<RouteNode> route;

        private RouteData(ArrayList<Long> waysIds, ArrayList<RouteNode> route) {
            this.waysIds = waysIds;
            this.route = route;
        }
    }

}
