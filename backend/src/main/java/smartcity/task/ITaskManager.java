package smartcity.task;

import routing.IGeoPosition;
import routing.IZone;

public interface ITaskManager {
    void scheduleCarCreation(int numberOfCars, int testCarId);
    Runnable getCreateCarTask(IGeoPosition start, IGeoPosition end, boolean testCar);
}
