package routing.data;

import routing.nodes.RouteNode;

import java.util.List;

public class RouteMergeInfo {
    public final List<RouteNode> startNodes;
    public final List<RouteNode> mergedRoute;
    public final List<RouteNode> newSimpleRouteEnd;
    public List<RouteNode> newUniformRoute;

    public RouteMergeInfo(List<RouteNode> startNodes, List<RouteNode> mergedRoute,
    		List<RouteNode> newSimpleRouteEnd) {
        this.startNodes = startNodes;
        this.mergedRoute = mergedRoute;
        this.newSimpleRouteEnd = newSimpleRouteEnd;
        newUniformRoute = null;
    }
}