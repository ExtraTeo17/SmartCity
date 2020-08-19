package smartcity.task;

import agents.VehicleAgent;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.IGeoPosition;
import routing.IZone;
import routing.RouteNode;
import routing.Router;
import smartcity.task.runnable.IRunnableFactory;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TaskManager {
    private final static Logger logger = LoggerFactory.getLogger(TaskManager.class);
    private final IRunnableFactory runnableFactory;

    @Inject
    TaskManager(IRunnableFactory runnableFactory) {
        this.runnableFactory = runnableFactory;
    }

    public void runNTimes(Consumer<Integer> runCountConsumer, int maxRunCount, int interval) {
        var runnable = runnableFactory.create(runCountConsumer, maxRunCount);
        runnable.runNTimes(interval, TimeUnit.MILLISECONDS);
    }

    public void scheduleCarCreation(int numberOfCars, IZone zone, int testCarId) {

    }
}
