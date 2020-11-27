package agents;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.inject.Inject;
import org.javatuples.Pair;

import com.google.common.eventbus.EventBus;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import osmproxy.buses.BrigadeInfo;
import osmproxy.buses.BusInfo;
import osmproxy.buses.Timetable;
import osmproxy.elements.OSMStation;
import smartcity.ITimeProvider;

import static agents.message.MessageManager.createProperties;
import static java.time.temporal.ChronoUnit.MILLIS;

public class BusManagerAgent extends AbstractAgent {

    private static final String NAME_PREFIX = "BusManager";
    private static final int ID = 1;
    public static final String NAME = NAME_PREFIX + ID;

    private HashSet<BusInfo> busInfos;

    public BusManagerAgent(ITimeProvider timeProvider, EventBus eventBus, HashSet<BusInfo> busInfos) {
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
                            logger.info("GET info from pedestrian about");
                            handleRouteQuery(rcv);
                            break;

                        }

                    }
                }
                block(100);
            }


            private void handleRouteQuery(ACLMessage rcv) {
                long stationOsmIdFrom = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.STATION_FROM_ID));
                long stationOsmIdTo = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.STATION_TO_ID));
                LocalTime arrivalTime = LocalTime.parse(rcv.getUserDefinedParameter(MessageParameter.ARRIVAL_TIME));
				String event = rcv.getUserDefinedParameter(MessageParameter.EVENT);
				ACLMessage msg = getBestMatch(rcv.createReply(), stationOsmIdFrom, stationOsmIdTo, arrivalTime, event,
						rcv.getUserDefinedParameter(MessageParameter.BUS_LINE),
						rcv.getUserDefinedParameter(MessageParameter.BRIGADE));
				send(msg);
                logger.info("Send message to pedestrian ");
            }

			private ACLMessage getBestMatch(ACLMessage response, long stationOsmIdFrom, long stationOsmIdTo,
					LocalTime timeOnStation, String event, String troubledLine, String troubledBrigade) {
				long minimumTimeOverall = Long.MAX_VALUE;
                String preferredBusLine = null;
                LocalTime TEST_preferredTimeOnStationFrom = null;
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
                        LocalTime minimumTimeOnStationFrom = null;
                        LocalTime minimumTimeOnStationTo = null;
                        for (BrigadeInfo brigInfo : info.brigadeList) {
                        	if (info.busLine.equals(troubledLine) && brigInfo.brigadeId.equals(troubledBrigade)) {
                        		continue;
                        	}
                            for (Timetable table : brigInfo.timetables) {
                                LocalTime timeOnStationFrom = table.getTimeOnStation(stationOsmIdFrom).get().toLocalTime();
                                LocalTime timeOnStationTo = table.getTimeOnStation(stationOsmIdTo).get().toLocalTime();

                                long timeInSeconds = differenceInSeconds(timeOnStationFrom, timeOnStation);
                                if (minimumTimeDistanceBetweenStationFromAndBusArrival > timeInSeconds) { // TODO: take stationTo into consideration
                                    minimumTimeDistanceBetweenStationFromAndBusArrival = timeInSeconds;
                                    minimumTimeOnStationFrom = timeOnStationFrom;
                                    minimumTimeOnStationTo = timeOnStationTo;
                                }
                            }
                        }
                        long overallTravelTime = minimumTimeDistanceBetweenStationFromAndBusArrival + differenceInSeconds(minimumTimeOnStationTo, minimumTimeOnStationFrom);
                        if (overallTravelTime < minimumTimeOverall) {
                            minimumTimeOverall = overallTravelTime;
                            preferredBusLine = info.busLine;
                            TEST_preferredTimeOnStationFrom = minimumTimeOnStationFrom;
                        }
                    }
                }
                logger.info("PREFERRED BUS LINE: " + preferredBusLine);
                logger.info("TIME ON STATION FROM: " + TEST_preferredTimeOnStationFrom);
                response.addUserDefinedParameter(MessageParameter.TIME_BETWEEN_PEDESTRIAN_AT_STATION_ARRIVAL_AND_REACHING_DESIRED_STOP,
                        minimumTimeOverall + "");
                response.addUserDefinedParameter(MessageParameter.BUS_LINE, preferredBusLine);
                response.addUserDefinedParameter(MessageParameter.TYPE, MessageParameter.BUS_MANAGER);
                response.addUserDefinedParameter(MessageParameter.EVENT, event);
                logger.info("Prepared message for pedestrian");
                return response;
            }

            private long differenceInSeconds(LocalTime time1, LocalTime time2) {
                return time1.isBefore(time2) ? Long.MAX_VALUE : Math.abs(MILLIS.between(time1, time2) / 1000);
            }
        };
        addBehaviour(communication);

    }


}
