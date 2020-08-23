package web.abstractions;


import routing.core.IGeoPosition;

import java.util.List;

public interface IWebService extends IStartable {
    void prepareSimulation(List<IGeoPosition> positions);

    void createCar(IGeoPosition position);
}
