package web.abstractions;


import routing.IGeoPosition;

import java.util.List;

public interface IWebService extends IStartable {
    void setZone(List<IGeoPosition> positions);
}
