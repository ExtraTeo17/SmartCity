package routing.abstractions;

import routing.RouteNode;

import java.util.List;

public interface IRouteTransformer {
    List<RouteNode> uniformRoute(List<RouteNode> route);
}
