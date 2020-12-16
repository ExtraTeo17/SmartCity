package agents;

import agents.abstractions.IAgentsFactory;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import osmproxy.buses.BusInfo;
import osmproxy.buses.Timetable;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMStation;
import routing.abstractions.IRouteGenerator;
import routing.abstractions.IRouteTransformer;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;
import smartcity.ITimeProvider;
import smartcity.config.ConfigContainer;
import smartcity.lights.abstractions.ICrossroadFactory;
import smartcity.stations.StationStrategy;
import smartcity.task.abstractions.ITaskProvider;
import vehicles.*;

import java.util.HashSet;
import java.util.List;

@SuppressWarnings("OverlyCoupledClass")
/**
 * The class responsible for creating all types of agents
 */
public
class AgentsFactory implements IAgentsFactory {
    private static final Logger logger = LoggerFactory.getLogger(AgentsFactory.class);

    private final IdGenerator idGenerator;
    private final ITimeProvider timeProvider;
    private final IRouteTransformer routeTransformer;
    private final IRouteGenerator routeGenerator;
    private final ICrossroadFactory crossroadFactory;
    private final EventBus eventBus;
    private final ConfigContainer configContainer;
    private final ITaskProvider taskProvider;

    @Inject
    public AgentsFactory(IdGenerator idGenerator,
                         EventBus eventBus,
                         ITimeProvider timeProvider,
                         IRouteTransformer routeTransformer,
                         ICrossroadFactory crossroadFactory,
                         IRouteGenerator routeGenerator,
                         ConfigContainer configContainer,
                         ITaskProvider taskProvider) {
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
        this.routeTransformer = routeTransformer;
        this.crossroadFactory = crossroadFactory;
        this.eventBus = eventBus;
        this.routeGenerator = routeGenerator;
        this.configContainer = configContainer;
        this.taskProvider = taskProvider;
    }

    /**
     * Creates fully filled car agent
     *
     * @param route   The defined route of vehicle
     * @param testCar Decision variable, showing is the object to be created should be Test
     * @return Filled {@link CarAgent} object.
     */
    @Override
    public CarAgent create(List<RouteNode> route, boolean testCar) {
        var id = idGenerator.get(CarAgent.class);
        var uniformRoute = routeTransformer.uniformRoute(route);
        logger.trace("DisplayRoute size: " + route.size() + ", routeSize: " + uniformRoute.size());
        var car = new Car(id, route, uniformRoute, timeProvider);
        if (testCar) {
            car = new TestCar(car, timeProvider);
        }

        return new CarAgent(id, car, timeProvider,
                routeGenerator, routeTransformer, eventBus, configContainer);
    }

    @Override
    public CarAgent create(List<RouteNode> route) {
        return create(route, false);
    }

    /**
     * Creates fully filled bike agent
     *
     * @param route    The defined route of vehicle
     * @param testBike Decision variable, showing is the object to be created should be Test
     * @return Filled {@link BikeAgent} object.
     */
    @Override
    public BikeAgent createBike(List<RouteNode> route, boolean testBike) {
        var id = idGenerator.get(CarAgent.class);
        var uniformRoute = routeTransformer.uniformRoute(route);
        logger.trace("DisplayRoute size: " + route.size() + ", routeSize: " + uniformRoute.size());
        var bike = new Bike(id, route, uniformRoute, timeProvider);
        if (testBike) {
            bike = new TestBike(bike);
        }

        return new BikeAgent(id, bike, timeProvider,
                eventBus);
    }

    @Override
    public BikeAgent createBike(List<RouteNode> route) {
        return createBike(route, false);
    }

    /**
     * Creates fully filled station manager agent
     *
     * @param station object, which contains essential information for work of StationAgent
     * @return Filled {@link StationAgent} object.
     */
    @Override
    public StationAgent create(OSMStation station) {
        var id = idGenerator.get(StationAgent.class);
        var stationStrategy = new StationStrategy(id, configContainer, timeProvider);
        return new StationAgent(id, station, stationStrategy, timeProvider, eventBus);
    }

    /**
     * Creates fully filled bus agent
     *
     * @param route     The defined route of vehicle
     * @param timetable Timetable of specific bus
     * @param busLine   Bus line number
     * @param brigadeNr Number of the brigade of the object
     * @return Filled {@link BusAgent} object.
     */
    @Override
    public BusAgent create(List<RouteNode> route, Timetable timetable, String busLine, String brigadeNr) {
        var id = idGenerator.get(BusAgent.class);
        var uniformRoute = routeTransformer.uniformRoute(route);
        logger.trace("DisplayRoute size: " + route.size() + ", routeSize: " + uniformRoute.size());
        var bus = new Bus(eventBus, timeProvider, id, route, uniformRoute, timetable, busLine, brigadeNr);
        return new BusAgent(id, bus, timeProvider, eventBus, configContainer);
    }

    @Deprecated
    @Override
    public LightManagerAgent create(Node crossroadNode) {
        var id = idGenerator.get(LightManagerAgent.class);
        var crossroad = crossroadFactory.create(id, crossroadNode);
        return new LightManagerAgent(id, crossroad, timeProvider, eventBus, configContainer);
    }

    /**
     * Creates fully filled LightManagerAgent
     *
     * @param centerCrossroad object,that conatins information about crossroad,
     *                        which will be managed by created Light Manager
     * @return Filled {@link LightManagerAgent} object.
     */
    @Override
    public LightManagerAgent create(OSMNode centerCrossroad) {
        var id = idGenerator.get(LightManagerAgent.class);
        var crossroad = crossroadFactory.create(id, centerCrossroad);
        return new LightManagerAgent(id, crossroad, timeProvider, eventBus, configContainer);
    }

    /**
     * Creates fully filled bus agent
     *
     * @param routeToStation   route from start point to startStation
     * @param routeFromStation route from finishStation to target point
     * @param startStation     start station of the pedestrian
     * @param finishStation    destination station
     * @param testPedestrian   if the created agent, should be test
     * @return Filled {@link PedestrianAgent} object.
     */
    // TODO: Simplify to avoid 6 arguments
    @Override
    public PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation,
                                  StationNode startStation, StationNode finishStation,
                                  boolean testPedestrian) {
        var id = idGenerator.get(PedestrianAgent.class);
        var uniformRouteToStation = routeTransformer.uniformRoute(routeToStation);
        var uniformRouteFromStation = routeTransformer.uniformRoute(routeFromStation);
        var pedestrian = new Pedestrian(id, routeToStation, uniformRouteToStation,
                routeFromStation, uniformRouteFromStation,
                startStation, finishStation,
                timeProvider, taskProvider);
        if (testPedestrian) {
            pedestrian = new TestPedestrian(pedestrian);
        }

        return new PedestrianAgent(id, pedestrian, timeProvider, taskProvider, eventBus, routeGenerator,
                configContainer);
    }

    @Override
    public PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation,
                                  StationNode startStation, StationNode finishStation) {
        return create(routeToStation, routeFromStation, startStation, finishStation, false);
    }

    /**
     * Creates fully filled BusManagerAgent
     *
     * @param busInfos information about schedules of buses and their stations
     * @return Filled {@link PedestrianAgent} object.
     */
    @Override
    public BusManagerAgent create(HashSet<BusInfo> busInfos) {
        return new BusManagerAgent(timeProvider, eventBus, busInfos);
    }
}
