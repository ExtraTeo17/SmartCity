package osmproxy.abstractions;

import osmproxy.buses.data.BusPreparationData;
import osmproxy.elements.OSMWay;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class responsible for retrieving and saving bus data from and to the cache.
 */
public interface ICacheWrapper {

	/**
	 * Retrieve bus preparation data from the cache.
	 *
	 * @return Bus preparation data ready to be utilised
	 */
    Optional<BusPreparationData> getBusPreparationData();

    /**
     * Save the bus preparation data to the cache
     *
     * @param data Bus preparation data ready to be utilised
     */
    void cacheData(BusPreparationData data);

    /**
     * Retrieve the route consisting of route nodes from the cache.
     *
     * @param route Route consisting of consecutive OSM ways to be used
     * for calculating the hash code for this particular route
     * @param stationNodes Station nodes residing on the route
     * @return Route consisting of route nodes ready to be utilised
     */
    ArrayList<RouteNode> getBusRoute(List<OSMWay> route,
                                     List<StationNode> stationNodes);

    /**
     * Save the route consisting of route nodes to the cache.
     *
     * @param route Route consisting of consecutive OSM ways to be used
     * for calculating the hash code for this particular route
     * @param stationNodes Station nodes residing on the route
     * @param data Route to be cached consisting of route nodes, ready to be utilised
     * after later retrieving from the cache
     */
    void cacheData(List<OSMWay> route, List<StationNode> stationNodes, ArrayList<RouteNode> data);
}
