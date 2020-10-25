package routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.abstractions.IRouteTransformer;
import routing.data.RouteMergeInfo;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;

import java.util.ArrayList;
import java.util.List;

import static utilities.NumericHelper.PRECISION;

final class RouteTransformer implements
        IRouteTransformer {
    private static final Logger logger = LoggerFactory.getLogger(RouteTransformer.class);

    // TODO: In some cases distance is 0 -> dx|dy is NaN -> same nodes?
    @SuppressWarnings("FeatureEnvy")
    @Override
    public List<RouteNode> uniformRoute(List<RouteNode> route) {
        List<RouteNode> newRoute = new ArrayList<>();
        for (int i = 0; i < route.size() - 1; ++i) {
            RouteNode nodeA = route.get(i);
            RouteNode nodeB = route.get(i + 1);

            double x = nodeB.getLng() - nodeA.getLng();
            double y = nodeB.getLat() - nodeA.getLat();

            double distance = RoutingHelper.getDistance(nodeA, nodeB);
            if (distance == 0) {
                continue;
            }

            double dx = x / distance;
            double dy = y / distance;

            double lon = nodeA.getLng();
            double lat = nodeA.getLat();
            newRoute.add(nodeA);
            for (int p = RoutingConstants.STEP_SIZE_METERS; p < distance; p += RoutingConstants.STEP_SIZE_METERS) {
                lon = lon + RoutingConstants.STEP_SIZE_METERS * dx;
                lat = lat + RoutingConstants.STEP_SIZE_METERS * dy;
                newRoute.add(new RouteNode(lat, lon, nodeA.getInternalEdgeId()));
            }
        }

        if (route.size() > 0) {
            newRoute.add(route.get(route.size() - 1));
        }

        return newRoute;
    }


    @Override
    public RouteMergeInfo mergeByDistance(List<RouteNode> oldRoute, List<RouteNode> newRoute) {
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
            result = newRoute;
        }

        return new RouteMergeInfo(oldNodes, result);
    }
}
