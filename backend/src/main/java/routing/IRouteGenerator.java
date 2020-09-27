package routing;

import com.google.common.annotations.Beta;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import routing.core.IGeoPosition;

import java.util.List;

public interface IRouteGenerator {
    List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB);

    // TODO: Merge with function for cars if testing proves they are identical
    @Deprecated
    List<RouteNode> generateRouteForPedestrians(IGeoPosition pointA, IGeoPosition pointB);

    // TODO: Improve routing to consider random OSM nodes as start/end points instead of random lat/lng
    // TODO: Always: either starting == null or finishing == null
    @Beta
    List<RouteNode> generateRouteForPedestrians(IGeoPosition pointA, IGeoPosition pointB,
                                                String startingOsmNodeRef, String finishingOsmNodeRef);

    List<RouteNode> generateRouteInfoForBuses(List<OSMWay> route, List<? extends OSMNode> osmStations);
}
