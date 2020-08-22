package smartcity.task;

import routing.core.IGeoPosition;

public interface ITaskManager {
    void scheduleCarCreation(int numberOfCars, int testCarId);

    Runnable getCreateCarTask(IGeoPosition start, IGeoPosition end, boolean testCar);
}
