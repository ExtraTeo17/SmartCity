package web.abstractions;


import routing.core.IGeoPosition;

import java.util.List;

public interface IWebService extends IStartable {
    void setZone(List<IGeoPosition> positions);

    void createCar(IGeoPosition position);
}
