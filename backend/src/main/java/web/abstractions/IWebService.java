package web.abstractions;


import osmproxy.elements.OSMNode;
import routing.core.IGeoPosition;
import smartcity.lights.core.Light;
import vehicles.Bus;
import vehicles.enums.BusFillState;

import java.util.List;

public interface IWebService extends IStartable {

    void prepareSimulation(List<? extends Light> lights,
                           List<? extends OSMNode> stations,
                           List<? extends Bus> buses);

    void startSimulation(int timeScale);

    void createCar(int id, IGeoPosition position, List<? extends IGeoPosition> route, boolean isTestCar);

    void updateCar(int id, IGeoPosition position);

    void killCar(int id);

    void updateLights(long lightGroupId);

    void createTroublePoint(int id, IGeoPosition position);

    void changeRoute(int agentId,
                     List<? extends IGeoPosition> routeStart,
                     IGeoPosition changePosition,
                     List<? extends IGeoPosition> routeEnd);

    void updateBus(int id, IGeoPosition position);

    void updateBusFillState(int id, BusFillState fillState);

    void killBus(int id);
}
