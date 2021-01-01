package web.abstractions;


import events.web.models.UpdateObject;
import osmproxy.elements.OSMStation;
import routing.core.IGeoPosition;
import smartcity.lights.core.Light;
import vehicles.Bus;
import vehicles.enums.BusFillState;

import java.util.List;

/**
 * Used for interaction with frontend interface.
 * Contains all method necessary to pass data or notify GUI.
 */
public interface IWebService extends IStartable {

    /**
     * Response to {@link web.message.payloads.requests.PrepareSimulationRequest}
     * @param lights - all generated lights in provided zone
     * @param stations - all generated bus stations in provided zone
     * @param buses - all generated buses (for all possible schedules and lines) in provided zone
     */
    void prepareSimulation(List<? extends Light> lights,
                           List<? extends OSMStation> stations,
                           List<? extends Bus> buses);

    void startSimulation();

    void createCar(int id, IGeoPosition position, List<? extends IGeoPosition> route, boolean isTestCar);

    void updateCar(int id, IGeoPosition position);

    void killCar(int id, int travelDistance, Long travelTime);

    void updateLights(long lightGroupId);

    void createTroublePoint(int id, IGeoPosition position);

    void hideTroublePoint(int id);

    void changeCarRoute(int agentId,
                        List<? extends IGeoPosition> routeStart,
                        IGeoPosition changePosition,
                        List<? extends IGeoPosition> routeEnd);

    void updateBus(int id, IGeoPosition position);

    void updateBusFillState(int id, BusFillState fillState);

    void killBus(int id);

    void createPedestrian(int id,
                          IGeoPosition position,
                          List<? extends IGeoPosition> routeToStation,
                          List<? extends IGeoPosition> routeFromStation,
                          boolean isTestPedestrian);

    void updatePedestrian(int id, IGeoPosition position);

    void pushPedestrianIntoBus(int id);

    void pullPedestrianAwayFromBus(int id, IGeoPosition position, boolean showRoute);

    void killPedestrian(int id, int travelDistance, Long travelTime);

    void startTrafficJam(long id);

    void endTrafficJam(long id);

    void createBike(int id, IGeoPosition position, List<? extends IGeoPosition> route, boolean isTestBike);

    void updateBike(int id, IGeoPosition position);

    void killBike(int id, int travelDistance, Long travelTime);

    void batchedUpdate(List<UpdateObject> carUpdates,
                       List<UpdateObject> bikeUpdates,
                       List<UpdateObject> busUpdates,
                       List<UpdateObject> pedUpdates);

    void crashBus(int id);

    void notifyApiOverload();
}
