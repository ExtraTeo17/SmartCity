package routing.abstractions;

import com.google.common.annotations.Beta;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import routing.RouteNode;
import routing.core.IGeoPosition;

import java.util.List;

public interface IRouteGenerator {
    List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB);

    // TODO: Merge with function for cars if testing proves they are identical
    List<RouteNode> generateRouteForPedestrians(IGeoPosition pointA, IGeoPosition pointB);

    @Beta
    List<RouteNode> generateRouteForPedestrians(IGeoPosition pointA, IGeoPosition pointB,
                                                String startingOsmNodeRef, String finishingOsmNodeRef);

    List<RouteNode> generateRouteInfoForBuses(List<OSMWay> route, List<? extends IGeoPosition> osmStations);
}
