package vehicles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.buses.Timetable;
import routing.LightManagerNode;
import routing.RouteNode;
import routing.Router;
import routing.StationNode;
import routing.core.IGeoPosition;
import smartcity.ITimeProvider;

import java.util.*;

public class Bus extends MovingObject {
    public static int CAPACITY_MID = 10;
    public static int CAPACITY_HIGH = 25;

    private final Logger logger;
    private final Timetable timetable;
    private final HashMap<Integer, List<String>> stationsForPassengers;
    private final List<StationNode> stationNodesOnRoute;
    private final String busLine;
    private final List<RouteNode> displayRoute;
    private final List<RouteNode> route;
    private final ITimeProvider timeProvider;

    private DrivingState state = DrivingState.STARTING;
    private int moveIndex = 0;
    private int closestLightIndex = -1;
    private int closestStationIndex = -1;
    private int passengersCount = 0;

    // TODO: Factory for vehicles - inject
    public Bus(ITimeProvider timeProvider,
               List<RouteNode> route, Timetable timetable, String busLine,
               String brigadeNr) {
        super(40);
        this.timeProvider = timeProvider;
        this.displayRoute = route;
        this.timetable = timetable;
        this.busLine = busLine;
        this.logger = LoggerFactory.getLogger(Bus.class.getName() + " (l_" + busLine + ") (br_" + brigadeNr + ")");

        this.route = Router.uniformRoute(displayRoute);
        this.stationsForPassengers = new HashMap<>();
        this.stationNodesOnRoute = new ArrayList<>();
        for (RouteNode node : route) {
            if (node instanceof StationNode) {
                StationNode station = (StationNode) node;
                stationsForPassengers.put(station.getStationAgentId(), new ArrayList<>());
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
        var timeOnStation = timetable.getTimeOnStation(Long.parseLong(osmStationId));
        if (timeOnStation.isEmpty()) {
            logger.warn("Could not retrieve time for " + osmStationId);
        }

        return timeOnStation;
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

    // TODO: Are they though?
    //  Suppose Bus.move task is late by 5ms because of lags (performance issues)
    //  Then his speed is actually 3600 / (9ms + 5ms) = 257 / TIME_SCALE = 25.7 instead of 40
    //  This calculation is highly dependent on processor speed :(
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

    public boolean shouldStart() {
        var dateNow = timeProvider.getCurrentSimulationTime();
        var boardingTime = timetable.getBoardingTime();
        long hours = boardingTime.getHours();
        long minutes = boardingTime.getMinutes();

        return hours == dateNow.getHours() && minutes == dateNow.getMinutes();
    }
}
