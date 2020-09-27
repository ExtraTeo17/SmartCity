package agents;

import agents.abstractions.IAgentsFactory;
import com.google.inject.Inject;
import org.w3c.dom.Node;
import osmproxy.buses.Timetable;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMStation;
import routing.RouteNode;
import routing.StationNode;
import smartcity.ITimeProvider;
import vehicles.*;

import java.util.List;

class AgentsFactory implements IAgentsFactory {
    private final IdGenerator idGenerator;
    private final ITimeProvider timeProvider;

    @Inject
    public AgentsFactory(IdGenerator idGenerator,
                         ITimeProvider timeProvider) {
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public VehicleAgent create(List<RouteNode> route, boolean testCar) {
        MovingObjectImpl car = testCar ?
                new TestCar(route, timeProvider) :
                new MovingObjectImpl(route);
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
        var bus = new Bus(timeProvider,
                route, timetable, busLine, brigadeNr);
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
                                  StationNode startStation, StationNode finishStation, boolean testPedestrian) {
        var pedestrian = testPedestrian ?
                new TestPedestrian(routeToStation, routeFromStation, preferredBusLine, startStation, finishStation, timeProvider) :
                new Pedestrian(routeToStation, routeFromStation, preferredBusLine, startStation, finishStation);
        return new PedestrianAgent(idGenerator.get(PedestrianAgent.class), timeProvider, pedestrian);
    }

    @Override
    public PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation, String preferredBusLine,
                                  StationNode startStation, StationNode finishStation) {
        return create(routeToStation, routeFromStation, preferredBusLine, startStation, finishStation, false);
    }
}
