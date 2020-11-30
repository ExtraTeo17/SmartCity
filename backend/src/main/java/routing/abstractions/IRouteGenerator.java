package routing.abstractions;

import osmproxy.elements.OSMWay;
import routing.core.IGeoPosition;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;

import java.util.List;

public interface IRouteGenerator {
    default List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB, String typeOfVehicle) {
        return generateRouteInfo(pointA, pointB, typeOfVehicle, false);
    }

    default List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB, String typeOfVehicle,
                                              boolean bewareOfJammedEdge) {
        return generateRouteInfo(pointA, pointB, null, null, typeOfVehicle, bewareOfJammedEdge);
    }

    default List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB, boolean bewareOfJammedEdge) {
        return generateRouteInfo(pointA, pointB, null, null, "car", bewareOfJammedEdge);
    }

    List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB, String startingOsmNodeRef,
                                      String finishingOsmNodeRef, String typeOfVehicle, boolean bewareOfJammedEdge);

    default List<RouteNode> generateRouteForPedestrians(IGeoPosition pointA, IGeoPosition pointB,
                                                        String startingOsmNodeRef, String finishingOsmNodeRef) {
        return generateRouteInfo(pointA, pointB, startingOsmNodeRef, finishingOsmNodeRef, "foot", false);
    }

    List<RouteNode> generateRouteInfoForBuses(List<OSMWay> route, List<StationNode> stationNodes);

}
