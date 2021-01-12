package routing.abstractions;

import osmproxy.elements.OSMWay;
import routing.core.IGeoPosition;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;

import java.util.List;

/**
 * Helper for generating routes with customized properties.
 */
public interface IRouteGenerator {

    /**
     * Generate the list of {@link RouteNode} elements tracing the route from point
     * A to point B. The routing is OSM-Node-based, which means that the route
     * consists of OSM Nodes, called non-virtual nodes. The route is also uniformed
     * by default, which means that there are additional nodes, called virtual
     * nodes, which are added for brevity so that the distances between each
     * consecutive nodes are close to constant, so that later on it is easy to
     * measure velocity of the vehicles passing the routes.
     *
     * @param pointA              The geographical coordinates of the departure
     *                            place.
     * @param pointB              The geographical coordinates of the arrival place.
     * @param typeOfVehicle       The type of vehicle to be passed to the
     *                            GraphHopper routing API, such as "foot", "car",
     *                            "bike".
     * @param startStation        Concerns pedestrians' routes: the station to which
     *                            the pedestrian goes to in order to enter a bus
     *                            there
     * @param endStation          Concerns pedestrians' routes: the station on which
     *                            the pedestrian disembarks from the bus to continue
     *                            the journey to the destination by foot.
     * @param bewareOfJammedEdge  Parameter which defines, whether the route should
     *                            avoid the GraphHopper edges, which are at the time
     *                            being added to the forbidden edge set.
     * @return List of {@link RouteNode} elements containing the desired route.
     */
    List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB, String typeOfVehicle,
            StationNode startStation, StationNode endStation, boolean bewareOfJammedEdge);

    default List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB, String typeOfVehicle) {
        return generateRouteInfo(pointA, pointB, typeOfVehicle, null, null, false);
    }

    default List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB, boolean bewareOfJammedEdge) {
        return generateRouteInfo(pointA, pointB, "car", null, null, bewareOfJammedEdge);
    }

    default List<RouteNode> generateRouteForPedestrians(IGeoPosition pointA, IGeoPosition pointB, StationNode startStation,
            StationNode endStation) {
        return generateRouteInfo(pointA, pointB, "foot", startStation, endStation, false);
    }

    List<RouteNode> generateRouteInfoForBuses(List<OSMWay> route, List<StationNode> stationNodes);

}
