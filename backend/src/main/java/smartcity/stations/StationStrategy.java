package smartcity.stations;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.elements.OSMStation;
import routing.StationNode;
import smartcity.MasterAgent;
import smartcity.lights.OptimizationResult;

import java.time.Instant;
import java.util.*;

public class StationStrategy {
    private static final Logger logger = LoggerFactory.getLogger(StationStrategy.class);
    final private static boolean SHOULD_USE_STRATEGY = true;

    final private static int WAIT_PERIOD = 60;
    // AgentName - Schedule Arrival Time / Arrival Time
    final private Map<String, Pair<Instant, Instant>> farAwayBusMap = new HashMap<>();
    final private Map<String, Pair<Instant, Instant>> busOnStationMap = new HashMap<>();
    final private Map<String, String> busAgentNameToBusNumberMap = new HashMap<>();
    final private Map<String, PedestrianArrivalInfo> farAwayPedestrianMap = new HashMap<>();
    final private Map<String, PedestrianArrivalInfo> pedestrianOnStationMap = new HashMap<>();

    public StationStrategy(OSMStation station, int agentId) {
        var id = station.getId();
        MasterAgent.osmStationIdToStationNode.put(id,
                new StationNode(station.getLat(), station.getLng(),
                        Long.toString(id), agentId));
    }

    public void addBusToFarAwayQueue(String agentBusName, Instant arrivalTime, Instant scheduleArrivalTime) {
        farAwayBusMap.put(agentBusName, Pair.with(scheduleArrivalTime, arrivalTime));
    }

    public void addMappingOfBusAndTheirAgent(String agentBusName, String busNumber) {
        busAgentNameToBusNumberMap.put(agentBusName, busNumber);
    }

    public void addBusToQueue(String agentBusName, Instant arrivalTime, Instant scheduleArrivalTime) {
        busOnStationMap.put(agentBusName, Pair.with(scheduleArrivalTime, arrivalTime));
    }

    public void removeBusFromFarAwayQueue(String agentName) {
        farAwayBusMap.remove(agentName);
    }

    public void removeBusFromBusOnStationQueue(String agentName) {
        busOnStationMap.remove(agentName);
    }


    public void addPedestrianToFarAwayQueue(String agentName, String desiredBus, Instant arrivalTime) {
        if (!farAwayPedestrianMap.containsKey(desiredBus)) {

            farAwayPedestrianMap.put(desiredBus, new PedestrianArrivalInfo());
        }
        farAwayPedestrianMap.get(desiredBus).putPedestrianOnList(new Pair<>(agentName, arrivalTime));
    }

    public void addPedestrianToQueue(String agentName, String desiredBus, Instant arrivalTime) {
        if (!pedestrianOnStationMap.containsKey(desiredBus)) {

            pedestrianOnStationMap.put(desiredBus, new PedestrianArrivalInfo());
        }
        pedestrianOnStationMap.get(desiredBus).putPedestrianOnList(new Pair<>(agentName, arrivalTime));

    }

    public void removePedestrianFromFarAwayQueue(String agentName, String bus) {
        farAwayPedestrianMap.get(bus).agentNamesAndArrivalTimes.removeIf(x -> x.getValue0().equals(agentName));
    }

    public void removePedestrianFromBusOnStationQueue(String agentName, String bus) {
        try {
            pedestrianOnStationMap.get(bus).agentNamesAndArrivalTimes.removeIf(x -> x.getValue0().equals(agentName));
        } catch (Exception e) {
            removePedestrianFromFarAwayQueue(agentName, bus);
        }
    }


    public OptimizationResult requestBusesAndPeopleFreeToGo() {
        OptimizationResult result = new OptimizationResult();
        for (String bus : busOnStationMap.keySet()) {
            Pair<Instant, Instant> scheduleAndArrivalTime = busOnStationMap.get(bus);
            Date scheduleTime = Date.from(scheduleAndArrivalTime.getValue0());
            Date arrivalTime = Date.from(scheduleAndArrivalTime.getValue1());

            Calendar arrivalCalendar = Calendar.getInstance();
            arrivalCalendar.setTime(arrivalTime);

            Calendar scheduleCalendar = Calendar.getInstance();
            scheduleCalendar.set(arrivalCalendar.get(Calendar.YEAR), arrivalCalendar.get(Calendar.MONTH), arrivalCalendar.get(Calendar.DAY_OF_MONTH), scheduleTime.getHours(), scheduleTime.getMinutes());

            scheduleAndArrivalTime = new Pair<>(scheduleCalendar.toInstant(), arrivalTime.toInstant());

            if (scheduleAndArrivalTime.getValue1().isAfter(scheduleAndArrivalTime.getValue0().plusSeconds(StationStrategy.WAIT_PERIOD))) {

                StationStrategy.logger.info("------------------BUS WAS LATE-----------------------");
                List<String> passengersThatCanLeave = checkPassengersWhoAreReadyToGo(bus);
                result.addBusAndPedestrianGrantedPassthrough(bus, passengersThatCanLeave);
            }
            else if ((scheduleAndArrivalTime.getValue1().isAfter((scheduleAndArrivalTime.getValue0().minusSeconds(StationStrategy.WAIT_PERIOD))) &&
                    scheduleAndArrivalTime.getValue1().isBefore((scheduleAndArrivalTime.getValue0().plusSeconds(StationStrategy.WAIT_PERIOD))))) {
                StationStrategy.logger.info("------------------BUS WAS ON TIME-----------------------");
                List<String> passengersThatCanLeave = checkPassengersWhoAreReadyToGo(bus);
                List<String> farPassengers = new ArrayList<>();
                if (StationStrategy.SHOULD_USE_STRATEGY) {
                    farPassengers = checkPassengersWhoAreFar(bus, scheduleAndArrivalTime.getValue0().plusSeconds(StationStrategy.WAIT_PERIOD));
                }
                passengersThatCanLeave.addAll(farPassengers);
                result.addBusAndPedestrianGrantedPassthrough(bus, passengersThatCanLeave);
                StationStrategy.logger.info("-----------------WAITING FOR: " + farPassengers.size() + " PASSENGERS------------------");
            }
            else if (scheduleAndArrivalTime.getValue1().isBefore((scheduleAndArrivalTime.getValue0().minusSeconds(StationStrategy.WAIT_PERIOD)))) {
                StationStrategy.logger.info("------------------BUS TOO EARLY-----------------------");
            }
            else {
                StationStrategy.logger.info("------------------SOMETHING HAPPENED-----------------------");
            }
        }
        return result;
    }

    private List<String> checkPassengersWhoAreReadyToGo(String busAgentName) {
        String bus = busAgentNameToBusNumberMap.get(busAgentName);
        List<String> passengersThatCanLeave = new ArrayList<>();
        if (pedestrianOnStationMap.containsKey(bus)) {
            PedestrianArrivalInfo arrivalTime = pedestrianOnStationMap.get(bus);
            for (Pair<String, Instant> pedestrian : arrivalTime.agentNamesAndArrivalTimes) {
                passengersThatCanLeave.add(pedestrian.getValue0());
            }

            return passengersThatCanLeave;
        }
        return new ArrayList<>();
    }

    private List<String> checkPassengersWhoAreFar(String busAgentName, Instant deadline) {

        String bus = busAgentNameToBusNumberMap.get(busAgentName);
        List<String> passengersThatCanLeave = new ArrayList<>();
        if (farAwayPedestrianMap.containsKey(bus)) {
            PedestrianArrivalInfo arrivalTime = farAwayPedestrianMap.get(bus);
            for (Pair<String, Instant> pedestrian : arrivalTime.agentNamesAndArrivalTimes) {
                if (pedestrian.getValue1().isBefore(deadline)) {
                    StationStrategy.logger.info("--------Passenger too wait---------");
                    passengersThatCanLeave.add(pedestrian.getValue0());
                }
            }
            return passengersThatCanLeave;
        }
        return new ArrayList<>();

    }
}
