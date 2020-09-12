package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.LoggerLevel;
import agents.utilities.MessageParameter;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import jade.wrapper.AgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.LightManagerNode;
import routing.RouteNode;
import routing.Router;
import routing.StationNode;
import smartcity.ITimeProvider;
import smartcity.MasterAgent;
import utilities.ConditionalExecutor;
import utilities.Siblings;
import vehicles.Bus;
import vehicles.DrivingState;

import java.time.Instant;
import java.util.List;
import java.util.Random;

@SuppressWarnings("serial")
public class BusAgent extends AbstractAgent {
    private static final Logger logger = LoggerFactory.getLogger(BusAgent.class);
    private final ITimeProvider timeProvider;
    private final Bus bus;

    BusAgent(int busId,
             ITimeProvider timeProvider,
             Bus bus) {
        super(busId);
        this.timeProvider = timeProvider;
        this.bus = bus;
    }

    @Override
    public String getNamePrefix() {
        return "Bus";
    }

    @Override
    protected void setup() {
        informNextStation();
        StationNode station = bus.getCurrentStationNode();
        print("Started at station " + station.getStationId() + ".");
        bus.setState(DrivingState.MOVING);

        // TODO: Executed each x = 3600 / bus.getSpeed() = 3600m / (40 * TIME_SCALE) = 3600 / 400 = 9ms
        //   Maybe decrease the interval? - I don't think processor can keep up with all of this.
        Behaviour move = new TickerBehaviour(this, Router.STEP_CONSTANT / bus.getSpeed()) {
            @Override
            public void onTick() {
                if (bus.isAtTrafficLights()) {
                    switch (bus.getState()) {
                        case MOVING:
                            LightManagerNode light = bus.getCurrentTrafficLightNode();

                            ACLMessage msg = createMessage(ACLMessage.REQUEST_WHEN, LightManagerAgent.name,
                                    light.getLightManagerId());
                            Properties properties = new Properties();
                            properties.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(bus.getAdjacentOsmWayId()));
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                            bus.setState(DrivingState.WAITING_AT_LIGHT);
                            print("Asking LightManager" + light.getLightManagerId() + " for right to passage.");

                            break;
                        case WAITING_AT_LIGHT:

                            break;
                        case PASSING_LIGHT:
                            print("Passing the light.");
                            bus.move();
                            bus.setState(DrivingState.MOVING);
                            break;
                    }
                }
                else if (bus.isAtDestination()) {
                    bus.setState(DrivingState.AT_DESTINATION);
                    print("Reached destination.");

                    ACLMessage msg = createMessage(ACLMessage.INFORM, MasterAgent.name);
                    Properties prop = new Properties();
                    prop.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
                    prop.setProperty(MessageParameter.AT_DESTINATION, String.valueOf(Boolean.TRUE));
                    msg.setAllUserDefinedParameters(prop);
                    send(msg);
                    doDelete();
                }
                else if (bus.isAtStation()) {
                    switch (bus.getState()) {
                        case MOVING:
                            StationNode station = bus.getCurrentStationNode();
                            List<String> passengerNames = bus.getPassengers(station.getStationId());

                            if (passengerNames.size() > 0) {
                                ACLMessage leave = createMessage(ACLMessage.REQUEST, passengerNames);

                                Properties properties = new Properties();
                                properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
                                properties.setProperty(MessageParameter.STATION_ID, String.valueOf(station.getStationId()));
                                leave.setAllUserDefinedParameters(properties);
                                send(leave);
                            }


                            ACLMessage msg = createMessage(ACLMessage.REQUEST_WHEN, StationAgent.name, station.getStationId());
                            Properties properties = new Properties();
                            properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);

                            var timeOnStation = bus.getTimeOnStation(station.getOsmStationId());
                            timeOnStation.ifPresent(time -> properties.setProperty(MessageParameter.SCHEDULE_ARRIVAL,
                                    time.toInstant().toString()));
                            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + MasterAgent.getSimulationTime().toInstant());
                            msg.setAllUserDefinedParameters(properties);
                            print("Send REQUEST_WHEN to station");
                            send(msg);

                            print("Arrived at station " + station.getStationId() + ".");
                            bus.setState(DrivingState.WAITING_AT_STATION);
                            break;
                        case WAITING_AT_STATION:
                            // waiting for passengers...

                            // if you want to skip waiting (for tests) use this:
                            // bus.setState(DrivingState.PASSING_STATION);
                            break;
                        case PASSING_STATION:
                            RouteNode node = bus.findNextStop();
                            if (node instanceof LightManagerNode) {
                                informLightManager(bus);
                            }
                            informNextStation();

                            bus.setState(DrivingState.MOVING);
                            bus.move();
                            break;
                    }
                }
                else {
                    bus.move();
                }
            }
        };

        Behaviour communication = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv == null) {
                    return;
                }

                String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                if (type == null) {
                    return;
                }

                switch (type) {
                    case MessageParameter.LIGHT:
                        if (rcv.getPerformative() == ACLMessage.REQUEST) {
                            if (bus.getState() == DrivingState.WAITING_AT_LIGHT) {
                                ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());
                                Properties properties = new Properties();
                                properties.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
                                properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(bus.getAdjacentOsmWayId()));
                                response.setAllUserDefinedParameters(properties);
                                send(response);
                                if (bus.findNextStop() instanceof LightManagerNode) {
                                    informLightManager(bus);
                                }
                                bus.setState(DrivingState.PASSING_LIGHT);
                            }
                        }
                        break;
                    case MessageParameter.STATION:
                        if (rcv.getPerformative() == ACLMessage.REQUEST) {
                            if (bus.getState() == DrivingState.WAITING_AT_STATION) {
                                ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());

                                Properties properties = new Properties();
                                properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
                                response.setAllUserDefinedParameters(properties);
                                send(response);
                                logger.info("BUS: get REQUEST from station");
                                informNextStation();
                                bus.setState(DrivingState.PASSING_STATION);
                            }
                        }
                        break;
                    case MessageParameter.PEDESTRIAN:
                        switch (rcv.getPerformative()) {
                            case ACLMessage.REQUEST_WHEN:
                                int stationId =
                                        Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.STATION_ID));
                                print("Passenger " + rcv.getSender().getLocalName() + " entered the bus.",
                                        LoggerLevel.DEBUG);
                                bus.addPassengerToStation(stationId, rcv.getSender().getLocalName());

                                ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());

                                Properties properties = new Properties();
                                properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
                                response.setAllUserDefinedParameters(properties);
                                send(response);
                                print("Passengers: " + bus.getPassengersCount(), LoggerLevel.DEBUG);
                                break;
                            case ACLMessage.AGREE:
                                stationId = Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.STATION_ID));
                                print("Passenger " + rcv.getSender().getLocalName() + " left the bus.");
                                if (bus.removePassengerFromStation(stationId, rcv.getSender().getLocalName())) {
                                    print("Passengers: " + bus.getPassengersCount());
                                }
                                else {
                                    print("Removing passenger failed");
                                }
                                break;
                        }

                        break;
                }
                block(100);
            }
        };

        addBehaviour(move);
        addBehaviour(communication);
    }

    private void informNextStation() {
        // finds next station and announces his arrival
        var stationOpt = bus.findNextStation();
        if (stationOpt.isPresent()) {
            var station = stationOpt.get();
            var stationId = station.getStationId();
            ACLMessage msg = createMessage(ACLMessage.INFORM, StationAgent.name, stationId);

            Properties properties = new Properties();
            Instant currentTime = MasterAgent.getSimulationTime().toInstant();
            Instant time = currentTime.plusMillis(bus.getMillisecondsToNextStation());
            properties.setProperty(MessageParameter.ARRIVAL_TIME, time.toString());
            properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
            properties.setProperty(MessageParameter.BUS_LINE, bus.getLine());

            var osmId = station.getOsmStationId();
            var timeOnStation = bus.getTimeOnStation(osmId);
            if (timeOnStation.isPresent()) {
                properties.setProperty(MessageParameter.SCHEDULE_ARRIVAL, timeOnStation.get().toInstant().toString());
            }
            else {
                print("Could not retrieve time for " + stationId, LoggerLevel.ERROR);
                ConditionalExecutor.debug(this::logAllStations);
            }

            msg.setAllUserDefinedParameters(properties);

            send(msg);
            print("Sending INFORM to Station" + stationId);
        }
    }

    private void logAllStations() {
        print("Printing station nodes: ");
        var stations = bus.getStationNodesOnRoute();
        for (int i = 0; i < stations.size(); ++i) {
            var station = stations.get(i);
            var osmId = station.getOsmStationId();
            var stationId = station.getStationId();
            var timeOnStation = bus.getTimeOnStation(osmId);
            var timeString = timeOnStation.isPresent() ? timeOnStation.get().toString() : "";
            print(i + ": " + stationId + " on '" + timeString + "'");
        }
        print("Printing station nodes finished.");
    }

    public Bus getBus() {
        return bus;
    }

    public final String getLine() {
        return bus.getLine();
    }

    // TODO: Fix situation where bus route contains only one station and pedestrians tries to choose two
    public final Siblings<StationNode> getTwoSubsequentStations(final Random random) {
        List<StationNode> stationsOnRoute = bus.getStationNodesOnRoute();
        final int halfIndex = stationsOnRoute.size() / 2;
        return Siblings.of(stationsOnRoute.get(random.nextInt(halfIndex)),
                stationsOnRoute.get(halfIndex + random.nextInt(halfIndex)));
    }

    // TODO: New bus was created each time - check if nothing was broken?
    public void runBasedOnTimetable() {
        var state = this.getAgentState().getValue();
        if (state != AgentState.cAGENT_STATE_INITIATED) {
            print("I am not in initiated state. State: " + this.getAgentState().getName());
            return;
        }

        var date = timeProvider.getCurrentSimulationTime();
        long hours = bus.getBoardingTime().getHours();
        long minutes = bus.getBoardingTime().getMinutes();
        if (hours == date.getHours() && minutes == date.getMinutes()) {
            logger.debug("Running! (" + hours + ":" + minutes + ")");
            start();
        }
    }
}
