package osmproxy.routes;

import org.javatuples.Pair;
import routing.core.IGeoPosition;

import java.util.List;

public interface IHighwayAccessor {
    /**
     * Generate a route from point A to point B and return consecutive OSM way IDs throughout
     * generated route alongside the consecutive route edge IDs from the graph the route was
     * calculated on.
     *
     * @param from                 The point from which the route shall be generated.
     * @param to                   The point to which the route shall be generated.
     * @param typeOfVehicle        String representation of the transportation mode, e.g. "foot", "car", "bike"
     * @param bewareOfJammedRoutes Whether the graph edges forbidden in the
     *                             {@link AvoidEdgesRemovableWeighting} should be avoided throughout the generated route
     * @return A pair, in which first element is the consecutive OSM way ID list and the second
     * element is the consecutive GraphHopper graph edge ID list
     */
    Pair<List<Long>, List<Integer>> getOsmWayIdsAndEdgeList(IGeoPosition from,
                                                            IGeoPosition to,
                                                            String typeOfVehicle,
                                                            boolean bewareOfJammedRoutes);
}
