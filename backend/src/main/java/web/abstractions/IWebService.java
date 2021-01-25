package web.abstractions;


import events.web.models.UpdateObject;
import routing.core.IGeoPosition;
import routing.nodes.StationNode;
import smartcity.lights.core.Light;
import vehicles.Bus;
import vehicles.enums.BusFillState;

import java.util.List;

/**
 * Used for interaction with frontend interface. <br/>
 * Contains all method necessary to pass data or notify GUI.
 */
public interface IWebService extends IStartable {

    /**
     * Response to {@link web.message.payloads.requests.PrepareSimulationRequest}.
     *
     * @param lights   All generated lights in provided zone
     * @param stations All generated bus stations in provided zone
     * @param buses    All generated buses (for all possible schedules and lines) in provided zone
     */
    void prepareSimulation(List<? extends Light> lights,
                           List<? extends StationNode> stations,
                           List<? extends Bus> buses);

    /**
     * Response to {@link web.message.payloads.requests.StartSimulationRequest}.
     */
    void startSimulation();

    /**
     * Used to show new car agent.
     *
     * @param id        Agent id
     * @param position  Initial position
     * @param route     Initial route to display
     * @param isTestCar If is test car
     */
    void createCar(int id, IGeoPosition position, List<? extends IGeoPosition> route, boolean isTestCar);

    /**
     * Used to update car agent position. <br/>
     * There is no need to use this function when {@link #batchedUpdate(List, List, List, List)} method is used.
     *
     * @param id       Agent id
     * @param position Updated position
     */
    void updateCar(int id, IGeoPosition position);

    /**
     * Used to change car agent route dynamically.
     *
     * @param agentId        Car agent id
     * @param routeStart     Part of route before it is changed
     * @param changePosition Common point of routeStart and routeEnd
     * @param routeEnd       Part of route after it is changed
     */
    void changeCarRoute(int agentId,
                        List<? extends IGeoPosition> routeStart,
                        IGeoPosition changePosition,
                        List<? extends IGeoPosition> routeEnd);

    /**
     * Used to remove car agent.
     *
     * @param id             Agent id
     * @param travelDistance Travelled distance, i.e. length of route
     * @param travelTime     Travel time in simulation clock (may be null)
     */
    void killCar(int id, int travelDistance, Long travelTime);


    /**
     * Used to switch lights on the same crossing, i.e. lights that switch simultaneously.
     *
     * @param lightGroupId Light group id, i.e. id of lights that should switch simultaneously
     */
    void updateLights(long lightGroupId);

    /**
     * Used to show new trouble point.
     *
     * @param id       Unique trouble point id
     * @param position -
     */
    void createTroublePoint(int id, IGeoPosition position);

    /**
     * @param id Trouble point id
     */
    void hideTroublePoint(int id);

    /**
     * Used to update bike agent position. <br/>
     * There is no need to use this function when {@link #batchedUpdate(List, List, List, List)} method is used.
     *
     * @param id       Agent id
     * @param position Updated position
     */
    void updateBus(int id, IGeoPosition position);

    /**
     * @param id        Agent id
     * @param fillState Updated fill state
     */
    void updateBusFillState(int id, BusFillState fillState);

    /**
     * Used to remove bus agent.
     *
     * @param id Agent id
     */
    void killBus(int id);

    /**
     * Used to stop the bus and display rotating skull over it.
     *
     * @param id Agent id
     */
    void crashBus(int id);

    /**
     * Used to show new pedestrian agent.
     *
     * @param id               Agent id
     * @param position         Initial position
     * @param routeToStation   Route from initial position to initial bus station
     * @param routeFromStation Route from end bus station to end position
     * @param isTestPedestrian If is test pedestrian
     */
    void createPedestrian(int id,
                          IGeoPosition position,
                          List<? extends IGeoPosition> routeToStation,
                          List<? extends IGeoPosition> routeFromStation,
                          boolean isTestPedestrian);

    /**
     * Used to update pedestrian agent position. <br/>
     * There is no need to use this function when {@link #batchedUpdate(List, List, List, List)} method is used.
     *
     * @param id       Agent id
     * @param position Updated position
     */
    void updatePedestrian(int id, IGeoPosition position);

    /**
     * Used to hide the pedestrian and his route from station.
     *
     * @param id Agent id
     */
    void pushPedestrianIntoBus(int id);

    /**
     * Used to show the pedestrian and, optionally, his route to station.
     *
     * @param id        Agent id
     * @param position  Position where pedestrian should appear
     * @param showRoute If should should show route to station, if `false` then no route is displayed at all
     */
    void pullPedestrianAwayFromBus(int id, IGeoPosition position, boolean showRoute);

    /**
     * Used to remove pedestrian agent.
     *
     * @param id             Agent id
     * @param travelDistance Travelled distance, i.e. length of route
     * @param travelTime     Travel time in simulation clock (may be null)
     */
    void killPedestrian(int id, int travelDistance, Long travelTime);

    /**
     * @param id Unique traffic jam id
     */
    void startTrafficJam(long id);

    /**
     * @param id Traffic jam id
     */
    void endTrafficJam(long id);

    /**
     * Used to show new bike agent
     *
     * @param id         Agent id
     * @param position   Initial position
     * @param route      Initial route to display
     * @param isTestBike If is test bike
     */
    void createBike(int id, IGeoPosition position, List<? extends IGeoPosition> route, boolean isTestBike);

    /**
     * Used to update bike agent position. <br/>
     * There is no need to use this function when {@link #batchedUpdate(List, List, List, List)} method is used.
     *
     * @param id       Agent id
     * @param position Updated position
     */
    void updateBike(int id, IGeoPosition position);

    /**
     * Used to remove bike agent.
     *
     * @param id             Agent id
     * @param travelDistance Travelled distance, i.e. length of route
     * @param travelTime     Travel time in simulation clock (may be null)
     */
    void killBike(int id, int travelDistance, Long travelTime);

    /**
     * Used for collective position updates of agents. <br/>
     * There is no need to additionally use:
     * <ul>
     *     <li>{@link #updateCar(int, IGeoPosition)}</li>
     *     <li>{@link #updateBike(int, IGeoPosition)}</li>
     *     <li>{@link #updateBus(int, IGeoPosition)}</li>
     *     <li>{@link #updatePedestrian(int, IGeoPosition)}</li>
     * </ul>
     * when this method is used.
     *
     * @param carUpdates  List of all car agents positions.
     * @param bikeUpdates List of all bike agents positions.
     * @param busUpdates  List of all bus agents positions.
     * @param pedUpdates  List of all pedestrians agents positions.
     */
    void batchedUpdate(List<UpdateObject> carUpdates,
                       List<UpdateObject> bikeUpdates,
                       List<UpdateObject> busUpdates,
                       List<UpdateObject> pedUpdates);

    /**
     * Used to inform user when Overpass API is switched too often.
     */
    void notifyApiOverload();
}
