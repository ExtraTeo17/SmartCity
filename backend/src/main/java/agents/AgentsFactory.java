package agents;

import agents.abstractions.IAgentsFactory;
import com.google.inject.Inject;
import org.w3c.dom.Node;
import osmproxy.buses.Timetable;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMStation;
import routing.RouteNode;
import routing.StationNode;
import routing.abstractions.IRouteTransformer;
import smartcity.ITimeProvider;
import smartcity.lights.abstractions.ICrossroadFactory;
import smartcity.stations.StationStrategy;
import vehicles.*;

import java.util.List;

class AgentsFactory implements IAgentsFactory {
    private final IdGenerator idGenerator;
    private final ITimeProvider timeProvider;
    private final IRouteTransformer routeTransformer;
    private final ICrossroadFactory crossroadFactory;

    @Inject
    public AgentsFactory(IdGenerator idGenerator,
                         ITimeProvider timeProvider,
                         IRouteTransformer routeTransformer,
                         ICrossroadFactory crossroadFactory) {
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
        this.routeTransformer = routeTransformer;
        this.crossroadFactory = crossroadFactory;
    }

    @Override
    public VehicleAgent create(List<RouteNode> route, boolean testCar) {
        var uniformRoute = routeTransformer.uniformRoute(route);
        var car = new MovingObjectImpl(route, uniformRoute);
        if (testCar) {
            car = new TestCar(car, timeProvider);
        }

        return new VehicleAgent(idGenerator.get(VehicleAgent.class), car, timeProvider);
    }

    @Override
    public VehicleAgent create(List<RouteNode> route) {
        return create(route, false);
    }

    @Override
    public StationAgent create(OSMStation station) {
        var id = idGenerator.get(StationAgent.class);
        var stationStrategy = new StationStrategy(id);
        return new StationAgent(id, station, stationStrategy, timeProvider);
    }

    @Override
    public BusAgent create(List<RouteNode> route, Timetable timetable, String busLine, String brigadeNr) {
        var uniformRoute = routeTransformer.uniformRoute(route);
        var bus = new Bus(timeProvider, route, uniformRoute,
                timetable, busLine, brigadeNr);
        return new BusAgent(idGenerator.get(BusAgent.class), timeProvider, bus);
    }

    @Override
    public LightManagerAgent create(Node crossroadNode) {
        var id = idGenerator.get(LightManagerAgent.class);
        var crossroad = crossroadFactory.create(crossroadNode);
        return new LightManagerAgent(id, timeProvider, crossroad);
    }

    @Override
    public LightManagerAgent create(OSMNode centerCrossroad) {
        var id = idGenerator.get(LightManagerAgent.class);
        var crossroad = crossroadFactory.create(centerCrossroad);
        return new LightManagerAgent(id, timeProvider, crossroad);
    }

    // TODO: Simplify to avoid 6 arguments
    @Override
    public PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation, String preferredBusLine,
                                  StationNode startStation, StationNode finishStation,
                                  boolean testPedestrian) {
        var uniformRouteToStation = routeTransformer.uniformRoute(routeToStation);
        var uniformRouteFromStation = routeTransformer.uniformRoute(routeFromStation);
        var pedestrian = new Pedestrian(routeToStation, uniformRouteToStation,
                routeFromStation, uniformRouteFromStation,
                preferredBusLine,
                startStation, finishStation);
        if (testPedestrian) {
            pedestrian = new TestPedestrian(pedestrian, timeProvider);
        }

        return new PedestrianAgent(idGenerator.get(PedestrianAgent.class), timeProvider, pedestrian);
    }

    @Override
    public PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation, String preferredBusLine,
                                  StationNode startStation, StationNode finishStation) {
        return create(routeToStation, routeFromStation, preferredBusLine, startStation, finishStation, false);
    }
}
