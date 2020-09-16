package vehicles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.buses.Timetable;
import routing.LightManagerNode;
import routing.RouteNode;
import routing.Router;
import routing.StationNode;
import routing.core.IGeoPosition;

import java.util.*;

public class Bus extends MovingObject {
    public static int CAPACITY_MID = 10;
    public static int CAPACITY_HIGH = 25;
    private static final Logger logger = LoggerFactory.getLogger(Bus.class);

    private final Timetable timetable;
    private final HashMap<Integer, List<String>> stationsForPassengers;
    private final List<StationNode> stationNodesOnRoute;
    private final String busLine;
    private final List<RouteNode> displayRoute;
    private final List<RouteNode> route;

    private DrivingState state = DrivingState.STARTING;
    private int moveIndex = 0;
    private int closestLightIndex = -1;
    private int closestStationIndex = -1;
    private int passengersCount = 0;

    public Bus(List<RouteNode> route, Timetable timetable, String busLine,
               String brigadeNr) {
        super(40);
        this.displayRoute = route;
        this.timetable = timetable;
        this.busLine = busLine;

        this.route = Router.uniformRoute(displayRoute);
        this.stationsForPassengers = new HashMap<>();
        this.stationNodesOnRoute = new ArrayList<>();
        for (RouteNode node : route) {
            if (node instanceof StationNode) {
                StationNode station = (StationNode) node;
                stationsForPassengers.put(station.getStationId(), new ArrayList<>());
                stationNodesOnRoute.add(station);
            }
        }

        if (stationNodesOnRoute.size() < 2) {
            logger.warn("Only one station on route");
        }
    }

    public int getPassengersCount() {
        return passengersCount;
    }

    public void addPassengerToStation(int id, String name) {
        var passengers = stationsForPassengers.get(id);
        if (passengers != null) {
            passengers.add(name);
            ++passengersCount;
        }
        else {
            logger.warn("Unrecognized station id: " + id);
        }
    }

    public boolean removePassengerFromStation(int id, String name) {
        if (getPassengers(id).remove(name)) {
            --passengersCount;
            return true;
        }

        return false;
    }

    public List<String> getPassengers(int id) {
        var result = stationsForPassengers.get(id);
        if (result == null) {
            return new ArrayList<>();
        }

        return result;
    }

    public final String getLine() {
        return busLine;
    }

    public final List<StationNode> getStationNodesOnRoute() {
        return stationNodesOnRoute;
    }

    @Override
    public long getAdjacentOsmWayId() {
        return ((LightManagerNode) route.get(moveIndex)).getOsmWayId();
    }

    @Override
    public String getVehicleType() {
        return VehicleType.BUS.toString();
    }

    @Override
    public LightManagerNode getNextTrafficLight() {
        for (int i = moveIndex + 1; i < route.size(); i++) {
            if (route.get(i) instanceof LightManagerNode) {
                closestLightIndex = i;
                return getCurrentTrafficLightNode();
            }
        }
        closestLightIndex = -1;
        return getCurrentTrafficLightNode();
    }

    public Optional<StationNode> findNextStation() {
        for (int i = moveIndex + 1; i < route.size(); ++i) {
            if (route.get(i) instanceof StationNode) {
                closestStationIndex = i;
                return Optional.of((StationNode) route.get(i));
            }
        }
        closestStationIndex = -1;
        return Optional.empty();
    }

    public Optional<Date> getTimeOnStation(String osmStationId) {
        return timetable.getTimeOnStation(Long.parseLong(osmStationId));
    }

    public RouteNode findNextStop() {
        for (int i = moveIndex + 1; i < route.size(); i++) {
            if (route.get(i) instanceof StationNode) {
                return route.get(i);
            }
            else if (route.get(i) instanceof LightManagerNode) {
                return route.get(i);
            }
        }
        return null;
    }

    @Override
    public IGeoPosition getPosition() {
        return route.get(moveIndex);
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
        if (moveIndex == route.size()) {
            return false;
        }
        return route.get(moveIndex) instanceof LightManagerNode;
    }

    public boolean isAtStation() {
        if (moveIndex == route.size()) {
            return true;
        }
        return route.get(moveIndex) instanceof StationNode;
    }

    public StationNode getCurrentStationNode() {
        if (closestStationIndex == -1) {
            return null;
        }
        return (StationNode) (route.get(closestStationIndex));
    }

    @Override
    public boolean isAtDestination() {
        return moveIndex == route.size();
    }

    @Override
    public void move() {
        if (isAtDestination()) {
            moveIndex = 0;
        }
        else {
            moveIndex++;
        }
    }

    @Override
    public List<RouteNode> getDisplayRoute() {
        return displayRoute;
    }

    @Override
    public int getMillisecondsToNextLight() {
        return ((closestLightIndex - moveIndex) * Router.STEP_CONSTANT) / getSpeed();
    }

    public int getMillisecondsToNextStation() {
        return ((closestStationIndex - moveIndex) * Router.STEP_CONSTANT) / getSpeed();
    }

    @Override
    public DrivingState getState() {
        return state;
    }

    @Override
    public void setState(DrivingState state) {
        this.state = state;
    }

    // TODO: Replace with shouldStart()
    public Date getBoardingTime() {
        return timetable.getBoardingTime();
    }
}
