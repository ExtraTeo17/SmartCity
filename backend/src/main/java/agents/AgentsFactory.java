package agents;

import agents.abstractions.IAgentsFactory;
import com.google.inject.Inject;
import org.w3c.dom.Node;
import osmproxy.buses.Timetable;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMStation;
import routing.IRouteTransformer;
import routing.RouteNode;
import routing.StationNode;
import smartcity.ITimeProvider;
import vehicles.*;

import java.util.List;

class AgentsFactory implements IAgentsFactory {
    private final IdGenerator idGenerator;
    private final ITimeProvider timeProvider;
    private final IRouteTransformer routeTransformer;

    @Inject
    public AgentsFactory(IdGenerator idGenerator,
                         ITimeProvider timeProvider,
                         IRouteTransformer routeTransformer) {
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
        this.routeTransformer = routeTransformer;
    }

    @Override
    public VehicleAgent create(List<RouteNode> route, boolean testCar) {
        var uniformRoute = routeTransformer.uniformRoute(route);
        MovingObjectImpl car = testCar ?
                new TestCar(route, uniformRoute, timeProvider) :
                new MovingObjectImpl(route, uniformRoute);
        return new VehicleAgent(idGenerator.get(VehicleAgent.class), car, timeProvider);
    }

    @Override
    public VehicleAgent create(List<RouteNode> route) {
        return create(route, false);
    }

    @Override
    public StationAgent create(OSMStation osmStation) {
        return new StationAgent(idGenerator.get(StationAgent.class), timeProvider, osmStation);
    }

    @Override
    public BusAgent create(List<RouteNode> route, Timetable timetable, String busLine, String brigadeNr) {
        var uniformRoute = routeTransformer.uniformRoute(route);
        var bus = new Bus(timeProvider, route, uniformRoute,
                timetable, busLine, brigadeNr);
        return new BusAgent(idGenerator.get(BusAgent.class), timeProvider, bus);
    }

    @Override
    public LightManagerAgent create(Node crossroad) {
        return new LightManagerAgent(idGenerator.get(LightManagerAgent.class), timeProvider, crossroad);
    }

    @Override
    public LightManagerAgent create(OSMNode centerCrossroad) {
        return new LightManagerAgent(idGenerator.get(LightManagerAgent.class), timeProvider, centerCrossroad);
    }

    // TODO: Simplify to avoid 6 arguments
    @Override
    public PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation, String preferredBusLine,
                                  StationNode startStation, StationNode finishStation,
                                  boolean testPedestrian) {
        var uniformRouteToStation = routeTransformer.uniformRoute(routeToStation);
        var uniformRouteFromStation = routeTransformer.uniformRoute(routeFromStation);
        var pedestrian = testPedestrian ?
                new TestPedestrian(routeToStation, uniformRouteToStation,
                        routeFromStation, uniformRouteFromStation,
                        preferredBusLine, startStation, finishStation,
                        timeProvider) :
                new Pedestrian(routeToStation, uniformRouteToStation,
                        routeFromStation, uniformRouteFromStation,
                        preferredBusLine,
                        startStation, finishStation);
        return new PedestrianAgent(idGenerator.get(PedestrianAgent.class), timeProvider, pedestrian);
    }

    @Override
    public PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation, String preferredBusLine,
                                  StationNode startStation, StationNode finishStation) {
        return create(routeToStation, routeFromStation, preferredBusLine, startStation, finishStation, false);
    }
}
