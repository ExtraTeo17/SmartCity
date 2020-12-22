package routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.abstractions.IRouteTransformer;
import routing.data.RouteMergeInfo;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static utilities.NumericHelper.PRECISION;

public final class RouteTransformer implements // TODO: We'll make it private other time, sorry
        IRouteTransformer {
    private static final Logger logger = LoggerFactory.getLogger(RouteTransformer.class);

    // TODO: In some cases distance is 0 -> dx|dy is NaN -> same nodes?
    @SuppressWarnings("FeatureEnvy")
    @Override
    public List<RouteNode> uniformRoute(List<RouteNode> route) {
        return route; // TODO: CLEAN UP
    }

    @Override
    public List<RouteNode> uniformRouteNext(List<RouteNode> route) {
        List<RouteNode> newRoute = new ArrayList<>();
        List<RouteNode> doubledNodes = new ArrayList<>();

        for (int i = 0; i < route.size() - 1; ++i) {
            RouteNode nodeA = route.get(i);
            RouteNode nodeB = route.get(i + 1);

            double distance = RoutingHelper.getDistance(nodeA, nodeB);
            if (distance == 0) {
            	if (doubledNodes.isEmpty()) {
            		doubledNodes.add(nodeA);
            	}
            	doubledNodes.add(nodeB);
                continue;
			} else if (!doubledNodes.isEmpty()) {
				List<RouteNode> specialNodes = doubledNodes.stream()
						.filter(node -> node instanceof LightManagerNode || node instanceof StationNode)
						.collect(Collectors.toList());
				switch (specialNodes.size()) {
            		case 0:
            			nodeA = doubledNodes.get(0);
            			break;
            		case 1:
            			nodeA = specialNodes.get(0);
            			break;
            		default:
            			throw new IllegalStateException("There is more than one special node of same position on the route");
            	}
            	distance = RoutingHelper.getDistance(nodeA, nodeB);
            	doubledNodes.clear();
            }

            double x = nodeB.getLng() - nodeA.getLng();
            double y = nodeB.getLat() - nodeA.getLat();

            double dx = x / distance;
            double dy = y / distance;

            double lon = nodeA.getLng();
            double lat = nodeA.getLat();
            newRoute.add(nodeA);
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

    @Override
    public List<RouteNode> uniformRouteNew(List<RouteNode> route, List<Integer> edgeList) {
        List<RouteNode> newRoute = uniformRouteNext(route);

        double denominator = newRoute.size();
        for (double nominator = 0; nominator < denominator; ++nominator) {
            double percent = nominator / denominator;
            int index = (int) Math.round((percent * ((double) (edgeList.size()))));
            if (index == edgeList.size()) {
                --index;
            }
            newRoute.get((int) nominator).setInternalEdgeId(edgeList.get(index));
        }

        return newRoute;
    }


    @Override
    public RouteMergeInfo mergeByDistance(List<RouteNode> oldRoute, List<RouteNode> newRoute) {
        if (newRoute.isEmpty()) {
            throw new IllegalArgumentException("New route cannot be empty");
        }

        var beg = newRoute.get(0);
        int mergeIndex = oldRoute.size();
        for (int i = oldRoute.size() - 1; i > 0; --i) {
            if (oldRoute.get(i).distance(beg) < PRECISION) {
                mergeIndex = i;
                break;
            }
        }

        List<RouteNode> oldNodes;
        List<RouteNode> result;
        if (mergeIndex < oldRoute.size()) {
            oldNodes = oldRoute.subList(0, mergeIndex + 1);
            result = new ArrayList<>(oldNodes);
            result.addAll(newRoute);
        }
        else {
            oldNodes = new ArrayList<>();
            result = List.copyOf(newRoute);
        }

        return new RouteMergeInfo(oldNodes, result, newRoute);
    }
}
