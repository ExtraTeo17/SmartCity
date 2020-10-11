package web.abstractions;


import routing.RouteNode;
import routing.core.IGeoPosition;

import java.util.List;

public interface IWebService extends IStartable {
    void prepareSimulation(List<? extends IGeoPosition> positions);

    void createCar(int id, IGeoPosition position, List<? extends IGeoPosition> route, boolean isTestCar);

    void updateCar(int id, IGeoPosition position);

    void startSimulation(int timeScale);

    void killCar(int id);
}
