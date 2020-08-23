package smartcity.task;

import agents.VehicleAgent;
import agents.abstractions.IAgentsContainer;
import agents.abstractions.IAgentsFactory;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import events.VehicleAgentCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.RouteNode;
import routing.Router;
import routing.core.IGeoPosition;
import routing.core.IZone;
import routing.core.Position;
import smartcity.task.runnable.IRunnableFactory;
import utilities.NumericHelper;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TaskManager implements ITaskManager {
    private final static Logger logger = LoggerFactory.getLogger(TaskManager.class);
    private final static TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
    private final static int CREATE_CAR_INTERVAL = 500;

    private final IRunnableFactory runnableFactory;
    private final IAgentsFactory agentsFactory;
    private final IAgentsContainer agentsContainer;
    private final EventBus eventBus;
    private final IZone zone;

    private final Random random;
    private final Table<IGeoPosition, IGeoPosition, List<RouteNode>> routeInfoCache;

    @Inject
    TaskManager(IRunnableFactory runnableFactory,
                IAgentsFactory agentsFactory,
                IAgentsContainer agentsContainer,
                EventBus eventBus,
                IZone zone) {
        this.runnableFactory = runnableFactory;
        this.agentsFactory = agentsFactory;
        this.agentsContainer = agentsContainer;
        this.eventBus = eventBus;
        this.zone = zone;

        this.random = new Random();
        this.routeInfoCache = HashBasedTable.create();
    }

    public void runNTimes(Consumer<Integer> runCountConsumer, int runCount, int interval) {
        var runnable = runnableFactory.create(runCountConsumer, runCount);
        runnable.runNTimes(interval, TIME_UNIT);
    }

    @Override
    public void scheduleCarCreation(int numberOfCars, int testCarId) {
        Consumer<Integer> createCars = (Integer counter) -> {
            var zoneCenter = zone.getCenter();
            var geoPosInZoneCircle = generateRandomOffset(zone.getRadius());
            var posA = zoneCenter.sum(geoPosInZoneCircle);
            var posB = zoneCenter.diff(geoPosInZoneCircle);

            getCreateCarTask(posA, posB, counter == testCarId).run();
        };

        runNTimes(createCars, numberOfCars, CREATE_CAR_INTERVAL);
    }

    public Runnable getCreateCarTask(IGeoPosition start, IGeoPosition end, boolean testCar) {
        return () -> {
            List<RouteNode> info;
            try {
                info = routeInfoCache.get(start, end);
                if (info == null) {
                    info = Router.generateRouteInfo(start, end);
                    routeInfoCache.put(start, end, info);
                }
            } catch (Exception e) {
                logger.warn("Error generating route info", e);
                return;
            }

            VehicleAgent agent = agentsFactory.create(info, testCar);
            if (agentsContainer.tryAdd(agent)) {
                agent.start();
                eventBus.post(new VehicleAgentCreatedEvent(agent.getPosition()));
            }
        };
    }

    private IGeoPosition generateRandomOffset(int radius) {
        double angle = random.nextDouble() * Math.PI * 2;
        double lat = Math.sin(angle) * radius * Router.METERS_PER_DEGREE_INVERSE;
        double lng = Math.cos(angle) * radius * Router.METERS_PER_DEGREE_INVERSE * Math.cos(lat);
        return Position.of(lat, lng);
    }
}
