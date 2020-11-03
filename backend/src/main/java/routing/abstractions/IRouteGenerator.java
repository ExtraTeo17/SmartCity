package routing.abstractions;

import osmproxy.elements.OSMWay;
import routing.core.IGeoPosition;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;

import java.util.List;

public interface IRouteGenerator {
    List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB, String typeOfVehicle);

	List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB, String typeOfVehicle,
			boolean bewareOfJammedEdge);

	List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB, boolean bewareOfJammedEdge);

	List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB, String startingOsmNodeRef,
			String finishingOsmNodeRef, String typeOfVehicle, boolean bewareOfJammedEdge);

    List<RouteNode> generateRouteForPedestrians(IGeoPosition pointA, IGeoPosition pointB,
                                                String startingOsmNodeRef, String finishingOsmNodeRef);

    List<RouteNode> generateRouteInfoForBuses(List<OSMWay> route, List<StationNode> stationNodes);

}
