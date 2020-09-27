package smartcity.stations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.elements.OSMStation;
import routing.StationNode;
import smartcity.MasterAgent;
import smartcity.lights.OptimizationResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("UnusedReturnValue")
public class StationStrategy {
    private static final Logger logger = LoggerFactory.getLogger(StationStrategy.class);
    private final static boolean SHOULD_USE_STRATEGY = true;
    private final static int WAIT_PERIOD_SECONDS = 60;

    // AgentName - Schedule Arrival Time / Arrival Time
    private final Map<String, String> busAgentNameToLine = new HashMap<>();
    private final Map<String, ScheduledArrivalTime> busAgentOnStationToArrivalTime = new HashMap<>();
    private final Map<String, ScheduledArrivalTime> farAwayBusAgentNameToArrivalTime = new HashMap<>();

    private final Map<String, List<ArrivalInfo>> busLineToFarAwayPedestrians = new HashMap<>();
    private final Map<String, List<ArrivalInfo>> busLineToPedestriansOnStation = new HashMap<>();

    public StationStrategy(OSMStation station, int agentId) {
        // Temporary workaround, because of static container in MasterAgent
        if (station == null) {
            return;
        }

        var id = station.getId();
        MasterAgent.osmStationIdToStationNode.put(id,
                new StationNode(station.getLat(), station.getLng(), id, agentId));
    }

    public boolean addBusAgentWithLine(String agentName, String busLine) {
        return busAgentNameToLine.put(agentName, busLine) == null;
    }

    public boolean addBusToFarAwayQueue(String agentName, LocalDateTime scheduled, LocalDateTime actual) {
        return farAwayBusAgentNameToArrivalTime.put(agentName, ScheduledArrivalTime.of(scheduled, actual)) == null;
    }

    public boolean removeBusFromFarAwayQueue(String agentName) {
        return farAwayBusAgentNameToArrivalTime.remove(agentName) != null;
    }

    public boolean addBusToQueue(String agentName, LocalDateTime scheduled, LocalDateTime actual) {
        return busAgentOnStationToArrivalTime.put(agentName, ScheduledArrivalTime.of(scheduled, actual)) == null;
    }

    public boolean removeBusFromQueue(String agentName) {
        return busAgentOnStationToArrivalTime.remove(agentName) != null;
    }

    public void addPedestrianToFarAwayQueue(String agentName, String desiredBusLine, LocalDateTime arrivalTime) {
        var farAwayPedestriansForLine = busLineToFarAwayPedestrians
                .computeIfAbsent(desiredBusLine, key -> new ArrayList<>());
        var arrivalInfo = new ArrivalInfo(agentName, arrivalTime);
        farAwayPedestriansForLine.add(arrivalInfo);
    }

    public boolean removePedestrianFromFarAwayQueue(String agentName, String busLine) {
        var arrivalInfos = busLineToFarAwayPedestrians.get(busLine);
        if (arrivalInfos == null) {
            logger.warn("Tried to remove pedestrian from non-existing far-away-queue for " + busLine);
            return false;
        }

        return arrivalInfos.removeIf(arrivalInfo -> arrivalInfo.agentName.equals(agentName));
    }

    public void addPedestrianToQueue(String agentName, String desiredBusLine, LocalDateTime arrivalTime) {
        var pedestriansOnStation = busLineToPedestriansOnStation
                .computeIfAbsent(desiredBusLine, key -> new ArrayList<>());
        var arrivalInfo = new ArrivalInfo(agentName, arrivalTime);
        pedestriansOnStation.add(arrivalInfo);
    }

    public boolean removePedestrianFromQueue(String agentName, String busLine) {
        var arrivalInfos = busLineToPedestriansOnStation.get(busLine);
        if (arrivalInfos == null) {
            logger.warn("Tried to remove pedestrian from non-existing queue for " + busLine);
            return false;
        }

        return arrivalInfos.removeIf(arrivalInfo -> arrivalInfo.agentName.equals(agentName));
    }


    public OptimizationResult requestBusesAndPeopleFreeToGo() {
        var result = new OptimizationResult();
        for (var entry : busAgentOnStationToArrivalTime.entrySet()) {
            var busLine = entry.getKey();
            var scheduledArrival = entry.getValue();

            var scheduledTime = scheduledArrival.scheduled;
            var scheduledTimePlusWait = scheduledTime.plusSeconds(WAIT_PERIOD_SECONDS);
            var scheduledTimeMinusWait = scheduledTime.minusSeconds(WAIT_PERIOD_SECONDS);
            var actualTime = scheduledArrival.actual;

            if (actualTime.isAfter(scheduledTimePlusWait)) {
                logger.info("------------------BUS WAS LATE-----------------------");
                List<String> passengersThatCanLeave = getPassengersWhoAreReadyToGo(busLine);
                result.addBusAndPedestrianGrantedPassthrough(busLine, passengersThatCanLeave);
            }
            else if (actualTime.isAfter(scheduledTimeMinusWait) &&
                    actualTime.isBefore(scheduledTimePlusWait)) {
                logger.info("------------------BUS WAS ON TIME-----------------------");
                List<String> passengersThatCanLeave = getPassengersWhoAreReadyToGo(busLine);
                if (SHOULD_USE_STRATEGY) {
                    var farPassengers = getPassengersWhoAreFar(busLine, scheduledTime.plusSeconds(WAIT_PERIOD_SECONDS));
                    passengersThatCanLeave.addAll(farPassengers);
                    logger.info("-----------------WAITING FOR: " + farPassengers.size() + " PASSENGERS------------------");
                }

                result.addBusAndPedestrianGrantedPassthrough(busLine, passengersThatCanLeave);
            }
            else if (actualTime.isBefore(scheduledTimeMinusWait)) {
                logger.info("------------------BUS TOO EARLY-----------------------");
            }
            else {
                logger.warn("Undetermined situation for line " + busLine + ", scheduledTime" + scheduledTime +
                        ", actualTime" + actualTime);
            }
        }

        return result;
    }

    private List<String> getPassengersWhoAreReadyToGo(String busAgentName) {
        String busLine = busAgentNameToLine.get(busAgentName);
        var arrivalInfos = busLineToPedestriansOnStation.get(busLine);
        if (arrivalInfos == null) {
            return new ArrayList<>();
        }

        return arrivalInfos.stream().map(info -> info.agentName).collect(Collectors.toList());
    }

    private List<String> getPassengersWhoAreFar(String busAgentName, LocalDateTime deadline) {
        String busLine = busAgentNameToLine.get(busAgentName);
        var arrivalInfos = busLineToFarAwayPedestrians.get(busLine);
        if (arrivalInfos == null) {
            return new ArrayList<>();
        }

        return arrivalInfos.stream()
                .filter(info -> info.arrivalTime.isBefore(deadline))
                .map(info -> info.agentName)
                .collect(Collectors.toList());
    }
}
