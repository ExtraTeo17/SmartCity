package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.CrashInfo;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import osmproxy.buses.BrigadeInfo;
import osmproxy.buses.BusInfo;
import osmproxy.buses.Timetable;
import osmproxy.elements.OSMStation;
import smartcity.ITimeProvider;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static agents.utilities.BehaviourWrapper.wrapErrors;
import static java.time.temporal.ChronoUnit.MILLIS;

/**
 * The class handles communication with bus agent and pedestrian agent in case of trouble on the road
 */
public class BusManagerAgent extends AbstractAgent {
    private static final String NAME_PREFIX = "BusManager";
    private static final int ID = 1;
    public static final String NAME = NAME_PREFIX + ID;

    private final HashSet<BusInfo> busInfos;

    List<CrashInfo> troubleCases = new ArrayList<>();

    BusManagerAgent(ITimeProvider timeProvider, EventBus eventBus, HashSet<BusInfo> busInfos) {
        super(ID, NAME_PREFIX, timeProvider, eventBus);
        this.busInfos = busInfos;
    }

    @Override
    protected void setup() {
        Behaviour communication = new CyclicBehaviour() {

            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv != null) {
                    switch (rcv.getPerformative()) {
                        case ACLMessage.INFORM -> {
                            if (rcv.getSender().getLocalName().contains("Bus")) {
                                initialiseCrash(rcv);
                            } else {
                                handleRouteQuery(rcv);
                            }
                        }
                    }
                }
                block(100);
            }

            private void initialiseCrash(ACLMessage rcv) {
                    troubleCases.add(new CrashInfo(LocalTime.parse(rcv.getUserDefinedParameter(MessageParameter.CRASH_TIME)),
                            rcv.getUserDefinedParameter(MessageParameter.BUS_LINE),
                            rcv.getUserDefinedParameter(MessageParameter.BRIGADE)));

            }

            private void handleRouteQuery(ACLMessage rcv) {
                long stationOsmIdFrom = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.STATION_FROM_ID));
                long stationOsmIdTo = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.STATION_TO_ID));
                LocalTime arrivalTime = LocalTime.parse(rcv.getUserDefinedParameter(MessageParameter.ARRIVAL_TIME));
                String event = rcv.getUserDefinedParameter(MessageParameter.EVENT);
                ACLMessage msg = getBestMatch(rcv.createReply(), stationOsmIdFrom, stationOsmIdTo, arrivalTime, event,
                        rcv.getUserDefinedParameter(MessageParameter.BUS_LINE),
                        rcv.getUserDefinedParameter(MessageParameter.BRIGADE),rcv);
                send(msg);


                logger.info("Send message to pedestrian");
            }

            private ACLMessage getBestMatch(ACLMessage response, long stationOsmIdFrom, long stationOsmIdTo,
                                            LocalTime timeOnStation, String event, String troubledLine, String troubledBrigade, ACLMessage rcv) {
                long minimumTimeOverall = Long.MAX_VALUE;
                String preferredBusLine = null;
                for (BusInfo info : busInfos) {
                    OSMStation stationFrom = null, stationTo = null;
                    for (OSMStation station : info.stops) {
                        if (station.getId() == stationOsmIdFrom) {
                            stationFrom = station;
                        }
                        if (station.getId() == stationOsmIdTo && stationFrom != null) {
                            stationTo = station;
                        }
                    }
                    if (stationFrom != null && stationTo != null) {
                        long minimumTimeDistanceBetweenStationFromAndBusArrival = Long.MAX_VALUE;
                        LocalTime minimumTimeOnStationFrom = LocalTime.MAX;
                        LocalTime minimumTimeOnStationTo = LocalTime.MIN;

                        for (BrigadeInfo brigInfo : info.brigadeList) {
                            if (checkCrashedBuses(info.busLine,brigInfo.brigadeId,rcv)) {
                                continue;
                            }
                            for (Timetable table : brigInfo.timetables) {
                                var timeOnStationFromOpt = table.getTimeOnStation(stationOsmIdFrom).orElse(LocalDateTime.MAX);
                                var timeOnStationToOpt = table.getTimeOnStation(stationOsmIdTo).orElse(LocalDateTime.MIN);
                                var timeOnStationFrom = timeOnStationFromOpt.toLocalTime();
                                var timeOnStationTo = timeOnStationToOpt.toLocalTime();

                                long timeInSeconds = differenceInSeconds(timeOnStationFrom, timeOnStation);
                                // TODO: take stationTo into consideration
                                if (minimumTimeDistanceBetweenStationFromAndBusArrival > timeInSeconds) {
                                    minimumTimeDistanceBetweenStationFromAndBusArrival = timeInSeconds;
                                    minimumTimeOnStationFrom = timeOnStationFrom;
                                    minimumTimeOnStationTo = timeOnStationTo;
                                }
                            }
                        }

                        long overallTravelTime = minimumTimeDistanceBetweenStationFromAndBusArrival
                                + differenceInSeconds(minimumTimeOnStationTo, minimumTimeOnStationFrom);
                        if (overallTravelTime < minimumTimeOverall) {
                            minimumTimeOverall = overallTravelTime;
                            preferredBusLine = info.busLine;
                        }
                    }
                }
                logger.info("Preferred bus line: " + preferredBusLine);
                response.addUserDefinedParameter(MessageParameter.TIME_BETWEEN_PEDESTRIAN_AT_STATION_ARRIVAL_AND_REACHING_DESIRED_STOP,
                        minimumTimeOverall + "");
                response.addUserDefinedParameter(MessageParameter.BUS_LINE, preferredBusLine);
                response.addUserDefinedParameter(MessageParameter.TYPE, MessageParameter.BUS_MANAGER);
                response.addUserDefinedParameter(MessageParameter.EVENT, event);
                logger.info("Prepared message for pedestrian");
                return response;
            }

            private boolean checkCrashedBuses(String busLine, String brigadeId, ACLMessage rcv) {
                for(CrashInfo crash : troubleCases)
                {
                    if(busLine.equals(crash.busLine) && brigadeId.equals(crash.brigade))
                    {


                        logger.info(rcv.getSender().getLocalName() + "------------------------------------busLine " + crash.busLine+" brigade " + crash.brigade);
                        return true;
                    }
                }
                return false;
            }

            private long differenceInSeconds(LocalTime time1, LocalTime time2) {
                return time1.isBefore(time2) ? Long.MAX_VALUE : Math.abs(MILLIS.between(time1, time2) / 1000);
            }
        };
        var onError = createErrorConsumer();
        addBehaviour(wrapErrors(communication, onError));
    }
}
