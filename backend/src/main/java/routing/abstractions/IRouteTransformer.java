package routing.abstractions;

import routing.data.RouteMergeInfo;
import routing.nodes.RouteNode;

import java.util.List;

public interface IRouteTransformer {
    List<RouteNode> uniformRoute(List<RouteNode> route);

    List<RouteNode> uniformRouteNext(List<RouteNode> route);

    List<RouteNode> uniformRouteNew(List<RouteNode> route, List<Integer> edgeList);

    RouteMergeInfo mergeByDistance(List<RouteNode> oldRoute,
                                   List<RouteNode> newRoute);
}
