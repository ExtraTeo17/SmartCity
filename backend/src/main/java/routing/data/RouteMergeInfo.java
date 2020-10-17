package routing.data;

import routing.nodes.RouteNode;

import java.util.List;

public  class RouteMergeInfo{
    public final List<RouteNode> startNodes;
    public final List<RouteNode> mergedRoute;

    public RouteMergeInfo(List<RouteNode> startNodes, List<RouteNode> mergedRoute) {
        this.startNodes = startNodes;
        this.mergedRoute = mergedRoute;
    }
}