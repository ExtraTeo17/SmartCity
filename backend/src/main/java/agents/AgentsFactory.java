package agents;

import agents.abstractions.IAgentsFactory;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
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
import vehicles.*;

import java.util.List;

class AgentsFactory implements IAgentsFactory {
    private static final Logger logger = LoggerFactory.getLogger(AgentsFactory.class);

    private final IdGenerator idGenerator;
    private final ITimeProvider timeProvider;
    private final IRouteTransformer routeTransformer;
    private final IRouteGenerator routeGenerator;
    private final ICrossroadFactory crossroadFactory;
    private final EventBus eventBus;
	private final ConfigContainer configContainer;
    //TODO: GET from GUI;
    private final int timeBeforeTrouble = 5000;

    @Inject
    public AgentsFactory(IdGenerator idGenerator,
                         EventBus eventBus,
                         ITimeProvider timeProvider,
                         IRouteTransformer routeTransformer,
                         ICrossroadFactory crossroadFactory,
                         IRouteGenerator routeGenerator,
                         ConfigContainer configContainer) {
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
        this.routeTransformer = routeTransformer;
        this.crossroadFactory = crossroadFactory;
        this.eventBus = eventBus;
        this.routeGenerator = routeGenerator;
        this.configContainer = configContainer;
    }

    @Override
    public VehicleAgent create(List<RouteNode> route, boolean testCar) {
        var id = idGenerator.get(VehicleAgent.class);
        var uniformRoute = routeTransformer.uniformRoute(route);
        logger.trace("DisplayRoute size: " + route.size() + ", routeSize: " + uniformRoute.size());
        var car = new Car(id, route, uniformRoute);
        if (testCar) {
            car = new TestCar(car, timeProvider);
        }

        return new VehicleAgent(id, car, timeBeforeTrouble, timeProvider,
        		routeGenerator, routeTransformer, eventBus, configContainer);
    }

    @Override
    public VehicleAgent create(List<RouteNode> route) {
        return create(route, false);
    }

    @Override
    public StationAgent create(OSMStation station) {
        var id = idGenerator.get(StationAgent.class);
        var stationStrategy = new StationStrategy(id);
        return new StationAgent(id, station, stationStrategy, timeProvider, eventBus);
    }

    @Override
    public BusAgent create(List<RouteNode> route, Timetable timetable, String busLine, String brigadeNr) {
        var id = idGenerator.get(BusAgent.class);
        var uniformRoute = routeTransformer.uniformRoute(route);
        logger.trace("DisplayRoute size: " + route.size() + ", routeSize: " + uniformRoute.size());
        var bus = new Bus(eventBus, timeProvider, id, route, uniformRoute, timetable, busLine, brigadeNr);
        return new BusAgent(id, bus, timeProvider, eventBus);
    }

    @Override
    public LightManagerAgent create(Node crossroadNode) {
        var id = idGenerator.get(LightManagerAgent.class);
        var crossroad = crossroadFactory.create(id, crossroadNode);
        return new LightManagerAgent(id, crossroad, timeProvider, eventBus);
    }

    @Override
    public LightManagerAgent create(OSMNode centerCrossroad) {
        var id = idGenerator.get(LightManagerAgent.class);
        var crossroad = crossroadFactory.create(id, centerCrossroad);
        return new LightManagerAgent(id, crossroad, timeProvider, eventBus);
    }

    // TODO: Simplify to avoid 6 arguments
    @Override
    public PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation, String preferredBusLine,
                                  StationNode startStation, StationNode finishStation,
                                  boolean testPedestrian) {
        var id = idGenerator.get(PedestrianAgent.class);
        var uniformRouteToStation = routeTransformer.uniformRoute(routeToStation);
        var uniformRouteFromStation = routeTransformer.uniformRoute(routeFromStation);
        var pedestrian = new Pedestrian(id, routeToStation, uniformRouteToStation,
                routeFromStation, uniformRouteFromStation,
                preferredBusLine,
                startStation, finishStation);
        if (testPedestrian) {
            pedestrian = new TestPedestrian(pedestrian, timeProvider);
        }

        return new PedestrianAgent(id, pedestrian, timeProvider, eventBus);
    }

    @Override
    public PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation, String preferredBusLine,
                                  StationNode startStation, StationNode finishStation) {
        return create(routeToStation, routeFromStation, preferredBusLine, startStation, finishStation, false);
    }
}
