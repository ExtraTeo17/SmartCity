package agents;

import agents.abstractions.IAgentsFactory;
import com.google.inject.Inject;
import org.w3c.dom.Node;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMStation;
import routing.RouteNode;
import routing.StationNode;
import smartcity.ITimeManager;
import smartcity.buses.Timetable;
import vehicles.*;

import java.util.List;

class AgentsFactory implements IAgentsFactory {
    private final IdGenerator idGenerator;
    private final ITimeManager timeManager;

    @Inject
    public AgentsFactory(IdGenerator idGenerator, ITimeManager timeManager) {
        this.idGenerator = idGenerator;
        this.timeManager = timeManager;
    }

    @Override
    public VehicleAgent create(List<RouteNode> route, boolean testCar) {
        MovingObjectImpl car = testCar ? new TestCar(route) : new MovingObjectImpl(route);
        return new VehicleAgent(idGenerator.get(VehicleAgent.class), car);
    }

    @Override
    public VehicleAgent create(List<RouteNode> route) {
        return create(route, false);
    }

    @Override
    public StationAgent create(OSMStation osmStation) {
        return new StationAgent(idGenerator.get(StationAgent.class), osmStation);
    }

    @Override
    public BusAgent create(List<RouteNode> route, Timetable timetable, String busLine, String brigadeNr) {
        var bus = new Bus(route, timetable, busLine, brigadeNr);
        return new BusAgent(idGenerator.get(BusAgent.class), timeManager, bus);
    }

    @Override
    public LightManager create(Node crossroad) {
        return new LightManager(idGenerator.get(LightManager.class), crossroad);
    }

    @Override
    public LightManager create(OSMNode centerCrossroad) {
        return new LightManager(idGenerator.get(LightManager.class), centerCrossroad);
    }

    // TODO: Simplify to avoid 6 arguments
    @Override
    public PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation, String preferredBusLine,
                                  StationNode startStation, StationNode finishStation, boolean testPedestrian) {
        var pedestrian = testPedestrian ?
                new TestPedestrian(routeToStation, routeFromStation, preferredBusLine, startStation, finishStation) :
                new Pedestrian(routeToStation, routeFromStation, preferredBusLine, startStation, finishStation);
        return new PedestrianAgent(idGenerator.get(PedestrianAgent.class), pedestrian);
    }

    @Override
    public PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation, String preferredBusLine,
                                  StationNode startStation, StationNode finishStation) {
        return create(routeToStation, routeFromStation, preferredBusLine, startStation, finishStation, false);
    }
}
