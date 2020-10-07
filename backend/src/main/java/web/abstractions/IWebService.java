package web.abstractions;


import routing.core.IGeoPosition;
import smartcity.lights.core.Light;

import java.util.List;

public interface IWebService extends IStartable {
    void prepareSimulation(List<? extends Light> positions);

    void createCar(int id, IGeoPosition position, boolean isTestCar);

    void updateCar(int id, IGeoPosition position);

    void startSimulation(int timeScale);
}
