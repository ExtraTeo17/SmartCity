package smartcity.lights.abstractions;

import smartcity.lights.OptimizationResult;
import smartcity.lights.core.Light;
import smartcity.stations.ArrivalInfo;

import java.util.List;

/**
 * The crossroad object, which contains traffic signallers and holds information
 * about the traffic participants, such as cars, bikes, buses and pedestrians,
 * which are currently waiting at the lights of the crossroad.
 */
@SuppressWarnings("UnusedReturnValue")
public interface ICrossroad {
    List<Light> getLights();

	/**
	 * Add car to close queue: inform that the car has approached the crossroad.
	 *
	 * @param adjacentWayId The OSM Way ID adjacent to the light which has been
	 *                      approached by the car.
	 * @param agentName     The agent name of the approaching car.
	 * @return true if the operation has been successful
	 */
	boolean addCarToQueue(long adjacentWayId, String agentName);

	/**
	 * Remove car from close queue: inform that the car has just passed the
	 * crossroad.
	 *
	 * @param adjacentWayId The OSM Way ID adjacent to the light which had the car
	 *                      in the queue.
	 * @return true if the operation has been successful
	 */
	boolean removeCarFromQueue(long adjacentWayId);

	/**
	 * Remove car from close queue -- inform that the car will approach a light in
	 * the near future.
	 *
	 * @param adjacentWayId The OSM Way ID adjacent to the light which the car will
	 *                      approach in the future.
	 * @param arrivalInfo   The information regarding the car's arrival.
	 * @return true if the operation has been successful
	 */
	boolean addCarToFarAwayQueue(long adjacentWayId, ArrivalInfo arrivalInfo);

	/**
	 * Remove car from close queue -- inform that the car is about to approach.
	 *
	 * @param adjacentWayId The OSM Way ID adjacent to the light which the car is
	 *                      about to approach.
	 * @param agentName     The agent name of the car.
	 * @return true if the operation has been successful
	 */
	boolean removeCarFromFarAwayQueue(long adjacentWayId, String agentName);

	/**
	 * Perform optimization at the crossroad -- determine which cars waiting at the
	 * light queues should be granted right to pass the crossroad.
	 *
	 * @param extendTimeSeconds The time which shall be the extension of the normal
	 *                          time of the traffic light being green
	 * @return The result of the performed optimizations.
	 */
	OptimizationResult requestOptimizations(int extendTimeSeconds);

	boolean addPedestrianToQueue(long adjacentWayId, String agentName);

	boolean removePedestrianFromQueue(long adjacentWayId);

	boolean addPedestrianToFarAwayQueue(long adjacentWayId, ArrivalInfo arrivalInfo);

    boolean removePedestrianFromFarAwayQueue(long adjacentWayId, String agentName);

    void startLifetime();
}

//TODO: Interface is too big and too specific, make it more general and move some methods to different interface
