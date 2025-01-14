package vehicles;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import events.web.bus.BusAgentFillStateUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.buses.Timetable;
import routing.RoutingConstants;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;
import smartcity.ITimeProvider;
import vehicles.enums.BusFillState;
import vehicles.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static vehicles.Constants.SPEED_SCALE;

@SuppressWarnings("ClassWithTooManyFields")
public class Bus extends MovingObject {

    private static final int DEFAULT_SPEED = (int) (50 * SPEED_SCALE);
    public static final int CAPACITY_MID = 10;
    public static final int CAPACITY_HIGH = 25;

    private final Logger logger;
    private final Timetable timetable;
    private final HashMap<Integer, List<String>> stationsForPassengers;
    private final List<StationNode> stationNodesOnRoute;
    private final String busLine;
    private final ITimeProvider timeProvider;
    private final EventBus eventBus;
    private final String brigadeNr;

    private BusFillState fillState;
    private int closestStationIndex = -1;
    private int passengersCount;

    // TODO: Factory for vehicles - inject
    public Bus(EventBus eventBus, ITimeProvider timeProvider, int agentId, List<RouteNode> simpleRoute,
               List<RouteNode> uniformRoute, Timetable timetable, String busLine, String brigadeNr) {
        super(timeProvider, agentId, DEFAULT_SPEED, uniformRoute, simpleRoute);
        this.brigadeNr = brigadeNr;
        this.timeProvider = timeProvider;
        this.eventBus = eventBus;
        this.timetable = timetable;
        this.busLine = busLine;
        this.fillState = BusFillState.LOW;
        this.logger = LoggerFactory.getLogger(Bus.class.getName() + " (l_" + busLine + ") (br_" + brigadeNr + ")");

        this.stationsForPassengers = new HashMap<>();
        this.stationNodesOnRoute = new ArrayList<>();
        for (RouteNode node : simpleRoute) {
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

    public List<String> getAllPassengers() {
        List<String> allPassengers = new ArrayList<>();
        var it = stationsForPassengers.entrySet().iterator();
        while (it.hasNext()) {
            var pair = it.next();
            allPassengers.addAll(pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
        return allPassengers;
    }

    @VisibleForTesting
    void increasePassengersCount() {
        ++passengersCount;
        if (passengersCount > CAPACITY_HIGH) {
            setFillState(BusFillState.HIGH);
        }
        else if (passengersCount > CAPACITY_MID) {
            setFillState(BusFillState.MID);
        }
    }

    @VisibleForTesting
    void decreasePassengersCount() {
        --passengersCount;
        if (passengersCount <= CAPACITY_MID) {
            setFillState(BusFillState.LOW);
        }
        else if (passengersCount <= CAPACITY_HIGH) {
            setFillState(BusFillState.MID);
        }
    }

    private void setFillState(BusFillState newState) {
        if (this.fillState != newState) {
            this.fillState = newState;
            eventBus.post(new BusAgentFillStateUpdatedEvent(agentId, newState));
        }
    }

    public BusFillState getFillState() {
        return fillState;
    }

    public void addPassengerToStation(int id, String name) {
        var passengers = stationsForPassengers.get(id);
        if (passengers != null) {
            passengers.add(name);
            increasePassengersCount();
        }
        else {
            logger.warn("Unrecognized station id: " + id);
        }
    }

    public boolean removePassengerFromStation(int id, String name) {
        if (getPassengers(id).remove(name)) {
            decreasePassengersCount();
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

    public List<StationNode> getStationNodesOnRoute() {
        return stationNodesOnRoute;
    }

    public int getStationNodesOnRouteSize() {
        return stationNodesOnRoute.size();
    }

    @Override
    public long getAdjacentOsmWayId() {
        return ((LightManagerNode) uniformRoute.get(moveIndex)).getAdjacentWayId();
    }

    @Override
    public String getVehicleType() {
        return VehicleType.BUS.toString();
    }

    /**
     * Find next stop belonging to the bus' route starting from the position
     * on which the bus is at the time being, assuming the bus is not just
     * starting its journey and is not at the beginning of its route.
     *
     * @return Station node of the next station, empty if no more stations
     * available on the route
     */
    public Optional<StationNode> findNextStation() {
        return findNextStation(false);
    }

    /**
     * Find next stop belonging to the bus' route starting from the position
     * on which the bus is at the time being.
     *
     * @param isStart Whether the bus is just starting its entire route.
     * @return Station node of the next station, empty if no more stations
     * available on the route
     */
    public Optional<StationNode> findNextStation(boolean isStart) {
        for (int i = isStart ? 0 : moveIndex + 1; i < uniformRoute.size(); ++i) {
            if (uniformRoute.get(i) instanceof StationNode) {
                closestStationIndex = i;
                return Optional.of((StationNode) uniformRoute.get(i));
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
            if (stationNodesOnRoute.size() <= 1) {
                logger.info("Cannot interpolate arrival time on last station because it is the only station on bus route");
                return Optional.empty();
            }
            if (stationNodesOnRoute.get(stationNodesOnRoute.size() - 1).getOsmId() == stationId) {
                timeOnStation = interpolateArrivalOnLastStation();
            }
            else {
                logger.warn("Could not retrieve time for non-last-on-route station of OSM ID: " + stationId);
            }
        }

        return timeOnStation;
    }

    private Optional<LocalDateTime> interpolateArrivalOnLastStation() {
        logger.info("Station with non-retrievable time is last on route, interpolate its arrival time");
        StationNode penultimateStation = stationNodesOnRoute.get(stationNodesOnRoute.size() - 2);
        StationNode lastStation = stationNodesOnRoute.get(stationNodesOnRoute.size() - 1);
        Optional<LocalDateTime> departureFromPenultimateStation =
                timetable.getTimeOnStation(penultimateStation.getOsmId());
        if (departureFromPenultimateStation.isEmpty()) {
            logger.warn("Could not retrieve time for non-last-on-route station while interpolating last station arrival time," +
                    "OSM ID of penultimate stop: " + departureFromPenultimateStation);
            return Optional.empty();
        }

        return Optional.ofNullable(interpolateArrivalOnLastStation(penultimateStation, lastStation, departureFromPenultimateStation.get()));
    }

    private LocalDateTime interpolateArrivalOnLastStation(StationNode penultimateStation,
                                                          StationNode lastStation, LocalDateTime departureFromPenultimateStation) {
        int penultimateStopIndexOnRoute = findIndexOfNodeOnRoute(penultimateStation);
        int lastStopIndexOnRoute = findIndexOfNodeOnRoute(lastStation);
        if (penultimateStopIndexOnRoute == -1 || lastStopIndexOnRoute == -1) {
            logger.error("Could not find penultimate and last stations on bus route");
            return null;
        }

        double millisFromPenStopToLastStop = getMillisecondsFromAToB(penultimateStopIndexOnRoute, lastStopIndexOnRoute);
        final double millisecondsInOneSecond = 1000;
        final double secondsInOneMinute = 60;
        logger.debug("Time in seconds journey between penultimate and last stop is estimated to be: " +
                (millisFromPenStopToLastStop / millisecondsInOneSecond * timeProvider.getTimeScale()));

        var timeOnStation = departureFromPenultimateStation.plusMinutes(Math.round(millisFromPenStopToLastStop /
                millisecondsInOneSecond / secondsInOneMinute * ((double) timeProvider.getTimeScale())));
        logger.info("Time on penultimate stop: " + departureFromPenultimateStation +
                ", interpolated time on last stop: " + timeOnStation);

        return timeOnStation;
    }

    public RouteNode findNextStop() {
        for (int i = moveIndex + 1; i < uniformRoute.size(); i++) {
            if (uniformRoute.get(i) instanceof StationNode) {
                return uniformRoute.get(i);
            }
            else if (uniformRoute.get(i) instanceof LightManagerNode) {
                return uniformRoute.get(i);
            }
        }
        return null;
    }

    public boolean isAtStation() {
        if (moveIndex == uniformRoute.size()) {
            return false;
        }
        return uniformRoute.get(moveIndex) instanceof StationNode;
    }

    public Optional<StationNode> getCurrentStationNode() {
        if (closestStationIndex == -1) {
            return Optional.empty();
        }
        return Optional.of((StationNode) (uniformRoute.get(closestStationIndex)));
    }

    @Override
    public void move() {
        ++moveIndex;
    }

    // Suppose Bus.move task is late by 5ms because of lags (performance issues)
    // Then his speed is actually 3600 / (9ms + 5ms) = 257 / TIME_SCALE = 25.7
    // instead of 40
    // This calculation is highly dependent on processor speed :(
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

    public String getSuperExtraString() {
        return "Brigade: " + brigadeNr + ", bus line: " + busLine;
    }

    public String getBrigade() {
        return brigadeNr;
    }

    public void printDebugInfo() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < uniformRoute.size(); ++i) {
            builder.append("R[")
                    .append(i)
                    .append("]: ")
                    .append(uniformRoute.get(i).getDebugString(uniformRoute.get(i) instanceof StationNode))
                    .append("; ");
        }

        logger.info("\n&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&" +
                "\nBus line: " + this.busLine +
                "\nDisplay route debug of size: " + uniformRoute.size() +
                "\n" + builder.toString() +
                "\n&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&"
        );
    }
}
