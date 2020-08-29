package vehicles;

import routing.LightManagerNode;
import routing.RouteNode;
import routing.Router;
import routing.StationNode;
import routing.core.IGeoPosition;
import smartcity.buses.Timetable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Bus extends MovingObject {
    public static int CAPACITY_MID = 10;
    public static int CAPACITY_HIGH = 25;

    private final Timetable timetable;
    private final HashMap<Integer, List<String>> stationsForPassengers = new HashMap<>();
    private final List<StationNode> stationNodesOnRoute;
    private final String busLine;
    private final String brigadeNr;
    private DrivingState state = DrivingState.STARTING;
    private final List<RouteNode> displayRoute;
    private final List<RouteNode> route;
    private int index = 0;
    private int closestLightIndex = -1;
    private int closestStationIndex = -1;
    private int passengersCount = 0;

    public Bus(List<RouteNode> route, Timetable timetable, String busLine,
               String brigadeNr) {
        super(40);
        this.displayRoute = route;

        for (RouteNode node : route) {
            if (node instanceof StationNode) {
                StationNode station = (StationNode) node;
                stationsForPassengers.put(station.getStationId(), new ArrayList<>());
            }
        }

        this.route = Router.uniformRoute(displayRoute);
        this.timetable = timetable;
        this.stationNodesOnRoute =route.stream().filter(node -> node instanceof StationNode)
                .map(node -> (StationNode)node).collect(Collectors.toList());
        this.busLine = busLine;
        this.brigadeNr = brigadeNr;
    }

    public int getPassengersCount() {
        return passengersCount;
    }

    public void addPassengerToStation(int id, String passenger) {
        stationsForPassengers.get(id).add(passenger);
        passengersCount++;
    }

    public boolean removePassengerFromStation(int id, String passenger) {
        if (stationsForPassengers.get(id).remove(passenger)) {
            passengersCount--;
            return true;
        }
        return false;
    }

    public List<String> getPassengersToLeave(int id) {
        return stationsForPassengers.get(id);
    }

    public final String getLine() {
        return busLine;
    }

    public final List<StationNode> getStationNodesOnRoute() {
        return stationNodesOnRoute;

    }

    @Override
    public long getAdjacentOsmWayId() {
        return ((LightManagerNode) route.get(index)).getOsmWayId();
    }

    @Override
    public String getVehicleType() {
        return VehicleType.BUS.toString();
    }

    @Override
    public LightManagerNode getNextTrafficLight() {
        for (int i = index + 1; i < route.size(); i++) {
            if (route.get(i) instanceof LightManagerNode) {
                closestLightIndex = i;
                return getCurrentTrafficLightNode();
            }
        }
        closestLightIndex = -1;
        return getCurrentTrafficLightNode();
    }

    public StationNode findNextStation() {
        for (int i = index + 1; i < route.size(); i++) {
            if (route.get(i) instanceof StationNode) {
                closestStationIndex = i;
                return (StationNode) route.get(i);
            }
        }
        closestStationIndex = -1;
        return null;
    }

    public Date getTimeOnStation(String osmStationId) {
        return timetable.getTimeOnStation(Long.parseLong(osmStationId));

    }


    public RouteNode findNextStop() {
        for (int i = index + 1; i < route.size(); i++) {
            if (route.get(i) instanceof StationNode) {
                return (StationNode) route.get(i);
            }
            if (route.get(i) instanceof LightManagerNode) {
                return (LightManagerNode) route.get(i);
            }
        }
        return null;
    }

    @Override
    public IGeoPosition getPosition() {
        return route.get(index);
    }

    @Override
    public LightManagerNode getCurrentTrafficLightNode() {
        if (closestLightIndex == -1) {
            return null;
        }
        return (LightManagerNode) (route.get(closestLightIndex));
    }

    @Override
    public boolean isAtTrafficLights() {
        if (index == route.size()) {
            return false;
        }
        return route.get(index) instanceof LightManagerNode;
    }

    public boolean isAtStation() {
        if (index == route.size()) {
            return true;
        }
        return route.get(index) instanceof StationNode;
    }

    public StationNode getCurrentStationNode() {
        if (closestStationIndex == -1) {
            return null;
        }
        return (StationNode) (route.get(closestStationIndex));
    }

    @Override
    public boolean isAtDestination() {
        return index == route.size();
    }

    @Override
    public void move() {
        if (isAtDestination()) {
            index = 0;
        }
        else {
            index++;
        }
    }

    @Override
    public List<RouteNode> getDisplayRoute() {
        return displayRoute;
    }

    @Override
    public int getMillisecondsToNextLight() {
        return ((closestLightIndex - index) * Router.STEP_CONSTANT) / getSpeed();
    }

    public int getMillisecondsToNextStation() {
        return ((closestStationIndex - index) * Router.STEP_CONSTANT) / getSpeed();
    }

    @Override
    public DrivingState getState() {
        return state;
    }

    @Override
    public void setState(DrivingState state) {
        this.state = state;
    }

    public Date getBoardingTime() {
        return timetable.getBoardingTime();
    }


}
