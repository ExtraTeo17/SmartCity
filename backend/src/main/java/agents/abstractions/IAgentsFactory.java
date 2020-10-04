package agents.abstractions;

import agents.*;
import org.w3c.dom.Node;
import osmproxy.buses.Timetable;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMStation;
import routing.RouteNode;
import routing.StationNode;

import java.util.List;

public interface IAgentsFactory {
    VehicleAgent create(List<RouteNode> route, boolean testCar);

    VehicleAgent create(List<RouteNode> route);

    StationAgent create(OSMStation station);

    BusAgent create(List<RouteNode> route, Timetable timetable, String busLine,
                    String brigadeNr);

    LightManagerAgent create(Node crossroad);

    LightManagerAgent create(OSMNode centerCrossroad);

    PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation,
                           String preferredBusLine, StationNode startStation, StationNode finishStation,
                           boolean testPedestrian);

    PedestrianAgent create(List<RouteNode> routeToStation, List<RouteNode> routeFromStation,
                           String preferredBusLine, StationNode startStation, StationNode finishStation);
}
