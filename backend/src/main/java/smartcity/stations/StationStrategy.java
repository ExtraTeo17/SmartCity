package smartcity.stations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.ITimeProvider;
import smartcity.config.abstractions.IStationConfigContainer;
import smartcity.lights.OptimizationResult;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("UnusedReturnValue")
public class StationStrategy {
    private final Logger logger;
    private final int waitPeriodSeconds;
    private final IStationConfigContainer configContainer;
    private final Map<String, List<String>> toWhichPassengersStrategyWaits = new HashMap<>();
    private final Map<String, Boolean> busesFreeToGo = new HashMap<>();
    private final ITimeProvider timeProvider;

    public StationStrategy(int managerId, IStationConfigContainer configContainer, ITimeProvider timeProvider) {
        this.logger = LoggerFactory.getLogger(this.getClass().getSimpleName() + managerId);
        this.waitPeriodSeconds = configContainer.getExtendWaitTime();
        this.configContainer = configContainer;
        this.timeProvider = timeProvider;
    }

    // AgentName - Schedule Arrival Time / Arrival Time
    private final Map<String, String> busAgentNameToLine = new HashMap<>();
    private final Map<String, ScheduledArrivalTime> busAgentOnStationToArrivalTime = new HashMap<>();
    private final Map<String, ScheduledArrivalTime> farAwayBusAgentNameToArrivalTime = new HashMap<>();

    private final Map<String, List<ArrivalInfo>> busLineToFarAwayPedestrians = new HashMap<>();
    private final Map<String, List<ArrivalInfo>> busLineToPedestriansOnStation = new HashMap<>();

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
            logger.warn("Tried to remove pedestrian: " + agentName + " from non-existing far-away-queue for " + busLine);
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
            logger.warn("Tried to remove pedestrian" + agentName + " from non-existing queue for " + busLine);
            return false;
        }

        return arrivalInfos.removeIf(arrivalInfo -> arrivalInfo.agentName.equals(agentName));
    }

    public OptimizationResult requestBusesAndPeopleFreeToGo() {
        // TODO: Inject config container
        //  #Przemek: What to inject here? waitTimeExtend is already in configContainer
        var result = new OptimizationResult(0, 0);
        for (var entry : busAgentOnStationToArrivalTime.entrySet()) {
            var busLine = entry.getKey();
            var scheduledArrival = entry.getValue();

            var scheduledDateTime = scheduledArrival.scheduled;
            var scheduledTime = LocalTime.of(scheduledDateTime.getHour(), scheduledDateTime.getMinute(),
                    0, 0);
            var scheduledTimePlusWait = scheduledTime.plusSeconds(waitPeriodSeconds);

            var scheduledTimeMinusWait = scheduledTime.minusSeconds(waitPeriodSeconds);


            var currentTime = timeProvider.getCurrentSimulationTime();

            var actualTime = LocalTime.of(currentTime.getHour(),
                    currentTime.getMinute(), 1, 0);   // scheduledArrival.actual;
            logger.debug("Scheduled time + seconds " + scheduledTimePlusWait);
            logger.debug("Actual time: " + actualTime);
            if (actualTime.isAfter(scheduledTimePlusWait)) {
                logger.debug("------------------BUS WAS LATE-----------------------");
                List<String> passengersThatCanLeave = getPassengersWhoAreReadyToGo(busLine);
                result.addBusAndPedestrianGrantedPassthrough(busLine, passengersThatCanLeave);
            }
            else if (actualTime.isAfter(scheduledTimeMinusWait) &&
                    actualTime.isBefore(scheduledTimePlusWait)) {
                logger.info("------------------BUS WAS ON TIME-----------------------");


                if (configContainer.isStationStrategyActive()) {
                    var farPassengers = getPassengersWhoAreFar(busLine, scheduledDateTime.plusSeconds(waitPeriodSeconds));
                    if (!busesFreeToGo.containsKey(busAgentNameToLine.get(busLine))) {
                        busesFreeToGo.put(busAgentNameToLine.get(busLine), false);
                        toWhichPassengersStrategyWaits.put(busAgentNameToLine.get(busLine), farPassengers);
                        logger.info("INIALISATION OF WAITING");
                        logger.info("far passangers" + farPassengers.size());

                    }

                    logger.debug("------------------NUMBER OF PASSENGERS TO WHICH WE WAIT " + toWhichPassengersStrategyWaits.get(busAgentNameToLine.get(busLine)).size());
                    List<String> passengersThatCanLeave = getPassengersWhoAreReadyToGo(busLine);
                    if (busesFreeToGo.containsKey(busAgentNameToLine.get(busLine))) {
                        if (busesFreeToGo.get(busAgentNameToLine.get(busLine))) {

                            result.addBusAndPedestrianGrantedPassthrough(busLine, passengersThatCanLeave);
                            toWhichPassengersStrategyWaits.remove(busAgentNameToLine.get(busLine));
                            busesFreeToGo.remove(busAgentNameToLine.get(busLine));
                        }
                    }
                    else if (!busesFreeToGo.containsKey(busAgentNameToLine.get(busLine))
                            && toWhichPassengersStrategyWaits.get(busAgentNameToLine.get(busLine)).size() == 0) {
                        logger.info("ZOSTALO 0 toWhichPassengersStrategyWaits");
                        result.addBusAndPedestrianGrantedPassthrough(busLine, passengersThatCanLeave);
                        toWhichPassengersStrategyWaits.remove(busAgentNameToLine.get(busLine));
                    }

                }

            }
            else if (actualTime.isBefore(scheduledTimeMinusWait)) {
                logger.debug("BUS TOO EARLY: scheduled: " + scheduledTimeMinusWait + ", actual: " + actualTime);
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
                .filter(info -> info.arrivalTime.toLocalTime().isBefore(deadline.toLocalTime()))
                .map(info -> info.agentName)
                .collect(Collectors.toList());
    }

    public void removeFromToWhomWaitMap(String agentName, String desiredBusLine) {
        if (toWhichPassengersStrategyWaits.containsKey(desiredBusLine)) {
            logger.info("DELETE FROM TO WHOM WE WAIT -  QUQUE");
            toWhichPassengersStrategyWaits.get(desiredBusLine).remove(agentName);
            //oWhichPassengersStrategyWaits.put(desiredBusLine, );
            if (toWhichPassengersStrategyWaits.get(desiredBusLine).size() == 0) {
                busesFreeToGo.put(desiredBusLine, true);
            }

        }

    }
}
