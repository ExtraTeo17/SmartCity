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
import java.util.*;

public class Bus extends MovingObject {
    public static final int CAPACITY_MID = 10;
    public static final int CAPACITY_HIGH = 25;

    private final Logger logger;
    private final Timetable timetable;
    private final HashMap<Integer, List<String>> stationsForPassengers;
    private final List<StationNode> stationNodesOnRoute;
    private final String busLine;
    private final ITimeProvider timeProvider;
    private final EventBus eventBus;

    private BusFillState fillState;
    private int closestStationIndex = -1;
    private int passengersCount = 0;

    // TODO: Factory for vehicles - inject
    public Bus(EventBus eventBus,
               ITimeProvider timeProvider,
               int agentId,
               List<RouteNode> simpleRoute,
               List<RouteNode> uniformRoute,
               Timetable timetable,
               String busLine,
               String brigadeNr) {
        super(timeProvider, agentId, 40, uniformRoute, simpleRoute);
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
    public List<String> getAllPassangers(){
        List<String> allPassengers = new ArrayList<>();
        Iterator it = stationsForPassengers.entrySet().iterator();
        while (it.hasNext()) {
            var pair = (Map.Entry)it.next();
            allPassengers.addAll((List<String>)pair.getValue());
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

    public final List<StationNode> getStationNodesOnRoute() {
        return stationNodesOnRoute;
    }

    @Override
    public long getAdjacentOsmWayId() {
        return ((LightManagerNode) uniformRoute.get(moveIndex)).getAdjacentWayId();
    }

    @Override
    public String getVehicleType() {
        return VehicleType.BUS.toString();
    }

    public Optional<StationNode> findNextStation() {
        for (int i = moveIndex + 1; i < uniformRoute.size(); ++i) {
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
            logger.warn("Could not retrieve time for " + stationId);
        }

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
