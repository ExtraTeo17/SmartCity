package vehicles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.buses.Timetable;
import routing.LightManagerNode;
import routing.RouteNode;
import routing.RoutingConstants;
import routing.StationNode;
import smartcity.ITimeProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class Bus extends MovingObject {
    public static final int CAPACITY_MID = 10;
    public static final int CAPACITY_HIGH = 25;

    private final Logger logger;
    private final Timetable timetable;
    private final HashMap<Integer, List<String>> stationsForPassengers;
    private final List<StationNode> stationNodesOnRoute;
    private final String busLine;
    private final List<RouteNode> displayRoute;
    private final ITimeProvider timeProvider;

    private int closestStationIndex = -1;
    private int passengersCount = 0;

    // TODO: Factory for vehicles - inject
    public Bus(ITimeProvider timeProvider,
               List<RouteNode> route,
               List<RouteNode> uniformRoute,
               Timetable timetable,
               String busLine,
               String brigadeNr) {
        super(40, uniformRoute);
        this.timeProvider = timeProvider;
        this.displayRoute = route;
        this.timetable = timetable;
        this.busLine = busLine;
        this.logger = LoggerFactory.getLogger(Bus.class.getName() + " (l_" + busLine + ") (br_" + brigadeNr + ")");

        this.stationsForPassengers = new HashMap<>();
        this.stationNodesOnRoute = new ArrayList<>();
        for (RouteNode node : route) {
            if (node instanceof StationNode) {
                StationNode station = (StationNode) node;
                stationsForPassengers.put(station.getAgentId(), new ArrayList<>());
                stationNodesOnRoute.add(station);
            }
        }

        if (stationNodesOnRoute.size() < 2) {
            logger.debug("Only one station on route");
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
        return ((LightManagerNode) route.get(moveIndex)).getAdjacentWayId();
    }

    @Override
    public String getVehicleType() {
        return VehicleType.BUS.toString();
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

    public Optional<LocalDateTime> getTimeOnStation(String stationId) {
        return getTimeOnStation(Long.parseLong(stationId));
    }

    public Optional<LocalDateTime> getTimeOnStation(long stationId) {
        var timeOnStation = timetable.getTimeOnStation(stationId);
        if (timeOnStation.isEmpty()) {
            logger.warn("Could not retrieve time for " + stationId);
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

    public boolean isAtStation() {
        if (moveIndex == route.size()) {
            return false;
        }
        return route.get(moveIndex) instanceof StationNode;
    }

    public Optional<StationNode> getCurrentStationNode() {
        if (closestStationIndex == -1) {
            return Optional.empty();
        }
        return Optional.of((StationNode) (route.get(closestStationIndex)));
    }

    @Override
    public void move() {
        if (isAtDestination()) {
            // TODO: why?
            moveIndex = 0;
        }
        else {
            ++moveIndex;
        }
    }

    @Override
    public List<RouteNode> getDisplayRoute() {
        return displayRoute;
    }

    @Override
    public int getMillisecondsToNextLight() {
        return ((closestLightIndex - moveIndex) * RoutingConstants.STEP_CONSTANT) / getSpeed();
    }

    // TODO: Are they though?
    //  Suppose Bus.move task is late by 5ms because of lags (performance issues)
    //  Then his speed is actually 3600 / (9ms + 5ms) = 257 / TIME_SCALE = 25.7 instead of 40
    //  This calculation is highly dependent on processor speed :(
    public int getMillisecondsToNextStation() {
        return ((closestStationIndex - moveIndex) * RoutingConstants.STEP_CONSTANT) / getSpeed();
    }

    public boolean shouldStart() {
        var dateNow = timeProvider.getCurrentSimulationTime();
        var boardingTime = timetable.getBoardingTime();
        long hours = boardingTime.getHour();
        long minutes = boardingTime.getMinute();

        return hours == dateNow.getHour() && minutes == dateNow.getMinute();
    }
}
