package web.abstractions;


import osmproxy.elements.OSMNode;
import routing.core.IGeoPosition;
import routing.nodes.RouteNode;
import smartcity.lights.core.Light;

import java.util.List;

public interface IWebService extends IStartable {
    void prepareSimulation(List<? extends Light> lights, List<? extends OSMNode> stations);

    void startSimulation(int timeScale);

    void createCar(int id, IGeoPosition position, List<? extends IGeoPosition> route, boolean isTestCar);

    void updateCar(int id, IGeoPosition position);

    void killCar(int id);

    void updateLights(long lightGroupId);

    void createTroublePoint(int id, IGeoPosition position);

    void changeRoute(int agentId, List<RouteNode> route, IGeoPosition changePosition);
}
