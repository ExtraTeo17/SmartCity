package routing;

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
import routing.abstractions.IRouteTransformer;
import routing.core.IGeoPosition;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

final class Router implements
        IRouteGenerator {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    private final IMapAccessManager mapAccessManager;
    private final INodesContainer nodesContainer;
    private final ICacheWrapper cacheWrapper;
    private final IRouteTransformer routeTransformer;

    @Inject
    public Router(IMapAccessManager mapAccessManager,
                  INodesContainer nodesContainer,
                  ICacheWrapper cacheWrapper,
                  IRouteTransformer routeTransformer) {
        this.mapAccessManager = mapAccessManager;
        this.nodesContainer = nodesContainer;
        this.cacheWrapper = cacheWrapper;
        this.routeTransformer = routeTransformer;
    }

	@Override // TODO: now with new route generation there is sometimes "failed to get adjacent osmwayid" error, check it out
    public List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB,
            		String startingOsmNodeRef, String finishingOsmNodeRef, String typeOfVehicle,
            		boolean bewareOfJammedEdge) {
		boolean isCar = typeOfVehicle.equals("car");
        var osmWayIdsAndEdgeList = findRoute(pointA, pointB, typeOfVehicle, bewareOfJammedEdge);
        var osmWayIds = osmWayIdsAndEdgeList.getValue0();
        var routeInfoOpt = mapAccessManager.getRouteInfo(osmWayIds, isCar); // TODO: refactor inside to throw exception if not car or pedestrian
        if (routeInfoOpt.isEmpty()) {
            logger.warn("Generating route failed because of empty routeInfo");
            return new ArrayList<>();
        }

        var routeInfo = routeInfoOpt.get();
        
        if (startingOsmNodeRef == null) {
        	startingOsmNodeRef = routeInfo.getFirst().findClosestNodeRefTo(pointA);
        }
        if (finishingOsmNodeRef == null) {
        	finishingOsmNodeRef = routeInfo.getLast().findClosestNodeRefTo(pointB);
        }
        
        routeInfo.determineRouteOrientationsAndFilterRelevantNodes(startingOsmNodeRef, finishingOsmNodeRef);
        var route = createRouteNodeList(routeInfo, isCar);
        return routeTransformer.uniformRouteNew(route, osmWayIdsAndEdgeList.getValue1());
    }

	private List<RouteNode> createRouteNodeList(RouteInfo routeInfo, boolean isCar) {
		List<RouteNode> routeNodes = new ArrayList<>();
        for (var way : routeInfo) {
            int waypointCount = way.getWaypointCount();
        	int lastLightManagerId = -1;
        	boolean straight = way.getRouteOrientation() == RouteOrientation.FRONT;
        	int startingIndex = straight ? 0 : waypointCount - 1;
        	int lastIndex = straight ? waypointCount : -1;
        	int increment = straight ? 1 : -1;
            for (int j = startingIndex; j != lastIndex; j += increment) {
                var node = getNode(way, way.getWaypoint(j), routeInfo, isCar);
                if (node instanceof LightManagerNode) {
                	int lightManagerId = ((LightManagerNode)node).getLightManagerId();
                	if (!(lightManagerId == lastLightManagerId)) {
                		routeNodes.add(node);
                		lastLightManagerId = lightManagerId;
                	}
                } else if (node != null) {
                	routeNodes.add(node);
                }
            }
        }

        return routeNodes;
    }

	private RouteNode getNode(OSMWay way, OSMWaypoint waypoint, RouteInfo routeInfo, boolean isCar) {
    	long wayId = way.getId();
        long nodeRefId = Long.parseLong(waypoint.getOsmNodeRef());
        if (routeInfo.remove(nodeRefId)) {
        	if (isCar) {
        		return nodesContainer.getLightManagerNode(wayId, nodeRefId);
        	} else {
        		return nodesContainer.getLightManagerNode(nodeRefId);
        	}
        }
        return new RouteNode(waypoint.getLat(), waypoint.getLng(), false);
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
    //  HELPERS - Most are deliverable :(
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

    private static Pair<List<Long>, List<Integer>> findRoute(IGeoPosition pointA,
    		IGeoPosition pointB, String typeOfVehicle, boolean bewareOfJammedEdge) {
        return osmproxy.HighwayAccessor.getOsmWayIdsAndEdgeList(pointA.getLat(), pointA.getLng(), pointB.getLat(),
                pointB.getLng(), typeOfVehicle, bewareOfJammedEdge);
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
