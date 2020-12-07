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
	 * @param startingOsmNodeRef  The OSM Node ID of the node which should be the
	 *                            first on the route. If null is passed, then the
	 *                            generator will automatically find the best match
	 *                            among OSM nodes found on this route.
	 * @param finishingOsmNodeRef The OSM Node ID of the node which should be the
	 *                            last on the route. If null is passed, then the
	 *                            generator will automatically find the best match
	 *                            among OSM nodes found on this route.
	 * @param typeOfVehicle       The type of vehicle to be passed to the
	 *                            GraphHopper routing API, such as "foot", "car",
	 *                            "bike".
	 * @param bewareOfJammedEdge  Parameter which defines, whether the route should
	 *                            avoid the GraphHopper edges, which are at the time
	 *                            being added to the forbidden edge set.
	 * @return List of {@link RouteNode} elements containing the desired route.
	 */
    List<RouteNode> generateRouteInfo(IGeoPosition pointA, IGeoPosition pointB, String startingOsmNodeRef,
                                      String finishingOsmNodeRef, String typeOfVehicle, boolean bewareOfJammedEdge);

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

    default List<RouteNode> generateRouteForPedestrians(IGeoPosition pointA, IGeoPosition pointB,
                                                        String startingOsmNodeRef, String finishingOsmNodeRef) {
        return generateRouteInfo(pointA, pointB, startingOsmNodeRef, finishingOsmNodeRef, "foot", false);
    }

    List<RouteNode> generateRouteInfoForBuses(List<OSMWay> route, List<StationNode> stationNodes);

}
