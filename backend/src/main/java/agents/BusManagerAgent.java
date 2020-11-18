package agents;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		super.setup();
		
		Behaviour communication = new CyclicBehaviour() {

			@Override
			public void action() {
				ACLMessage rcv = receive();
				if (rcv != null) {
					switch (rcv.getPerformative()) {
					case ACLMessage.INFORM:
						handleRouteQuery(rcv);
						break;
					}
				}
			}
			
			private void handleRouteQuery(ACLMessage rcv) {
				long stationOsmIdFrom = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.STATION_FROM_ID));
				long stationOsmIdTo = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.STATION_TO_ID));
				LocalTime arrivalTime = LocalTime.parse(rcv.getUserDefinedParameter(MessageParameter.ARRIVAL_TIME));
				send(getBestMatch(rcv.createReply(), stationOsmIdFrom, stationOsmIdTo, arrivalTime));
			}

			private ACLMessage getBestMatch(ACLMessage response, long stationOsmIdFrom, long stationOsmIdTo, LocalTime timeOnStation) {
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
						LocalTime minimumTimeOnStationFrom = null;
						LocalTime minimumTimeOnStationTo = null;
						for (BrigadeInfo brigInfo : info.brigadeList) {
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
						}
					}
				}
				response.addUserDefinedParameter(MessageParameter.TIME_BETWEEN_PEDESTRIAN_AT_STATION_ARRIVAL_AND_REACHING_DESIRED_STOP,
						minimumTimeOverall + "");
				response.addUserDefinedParameter(MessageParameter.BUS_LINE, preferredBusLine);
				return response;
			}
			
			private long differenceInSeconds(LocalTime time1, LocalTime time2) {
				return (time1.toNanoOfDay() -
						time2.toNanoOfDay()) / 1000000000L;
			}
			
		};
		
		addBehaviour(communication);
	}

	@Override
	protected void takeDown() {
		// TODO Auto-generated method stub
		super.takeDown();
	}
}
