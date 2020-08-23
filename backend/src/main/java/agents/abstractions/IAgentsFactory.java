package agents.abstractions;

import agents.BusAgent;
import agents.LightManager;
import agents.PedestrianAgent;
import agents.VehicleAgent;
import org.w3c.dom.Node;
import osmproxy.elements.OSMNode;
import routing.RouteNode;
import routing.StationNode;
import smartcity.buses.Timetable;

import java.util.List;

public interface IAgentsFactory {
    VehicleAgent create(List<RouteNode> route, boolean testCar);

    VehicleAgent create(List<RouteNode> route);

    BusAgent create(List<RouteNode> route, Timetable timetable, String busLine,
                    String brigadeNr);

    LightManager create(Node crossroad);

    LightManager create(OSMNode centerCrossroad);

    PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation,
                           String preferredBusLine, StationNode startStation, StationNode finishStation,
                           boolean testPedestrian);

    PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation,
                           String preferredBusLine, StationNode startStation, StationNode finishStation);
}
