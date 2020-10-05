package web.abstractions;


import routing.core.IGeoPosition;

import java.util.List;

public interface IWebService extends IStartable {
    void prepareSimulation(List<? extends IGeoPosition> positions);

    void createCar(int id, IGeoPosition position);
}
