package routing.abstractions;

import routing.data.RouteMergeInfo;
import routing.nodes.RouteNode;

import java.util.List;

/**
 * Module responsible for transforming an already prepared route into other
 * forms.
 */
public interface IRouteTransformer {

    @Deprecated
    List<RouteNode> uniformRoute(List<RouteNode> route);

    /**
     * Add additional nodes between existing nodes on the route, called virtual
     * nodes. The virtual nodes are added for brevity so that the distances between
     * each consecutive nodes are close to constant, so that later on it is easy to
     * measure velocity of the vehicles passing the routes.
     *
     * @param route The route for which the virtual nodes are to be added
     * @return Uniformed route with almost equal distances between consecutive nodes
     */
    List<RouteNode> uniformRouteNext(List<RouteNode> route);

    List<RouteNode> uniformRouteNew(List<RouteNode> route, List<Integer> edgeList);

    RouteMergeInfo mergeByDistance(List<RouteNode> oldRoute,
                                   List<RouteNode> newRoute);
}
