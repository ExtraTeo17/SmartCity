package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import jade.core.AID;
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
        getNextStation();
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
                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
                            msg.addReceiver(new AID("LightManager" + light.getLightManagerId(), AID.ISLOCALNAME));
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

                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID(MasterAgent.name, AID.ISLOCALNAME));
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
                            List<String> passengers = bus.getPassengers(station.getStationId());

                            if (passengers.size() != 0) {
                                ACLMessage leave = new ACLMessage(ACLMessage.REQUEST);

                                for (String name : passengers) {
                                    leave.addReceiver(new AID(name, AID.ISLOCALNAME));
                                }

                                Properties properties = new Properties();
                                properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
                                properties.setProperty(MessageParameter.STATION_ID, String.valueOf(station.getStationId()));
                                leave.setAllUserDefinedParameters(properties);
                                send(leave);
                            }


                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
                            msg.addReceiver(new AID("Station" + station.getStationId(), AID.ISLOCALNAME));
                            Properties properties = new Properties();
                            properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);

                            var timeOnStation = bus.getTimeOnStation(station.getOsmStationId());
                            timeOnStation.ifPresent(time -> properties.setProperty(MessageParameter.SCHEDULE_ARRIVAL,
                                    time.toInstant().toString()));
                            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + MasterAgent.getSimulationTime().toInstant());
                            logger.info("BUS: send REQUEST_WHEN to station");
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);

                            print("Arrived at station " + station.getStationId() + ".");
                            bus.setState(DrivingState.WAITING_AT_STATION);
                            break;
                        case WAITING_AT_STATION:
                            // waiting for passengers...

                            // if you want to skip waiting (for tests) use this:
                            //bus.setState(DrivingState.PASSING_STATION);
                            break;
                        case PASSING_STATION:
                            RouteNode node = bus.findNextStop();
                            if (node instanceof LightManagerNode) {
                                informLightManager(bus);
                            }
                            getNextStation();

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
                                ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                                response.addReceiver(rcv.getSender());
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
                                ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                                response.addReceiver(rcv.getSender());

                                Properties properties = new Properties();
                                properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
                                response.setAllUserDefinedParameters(properties);
                                send(response);
                                logger.info("BUS: get REQUEST from station");
                                getNextStation();
                                bus.setState(DrivingState.PASSING_STATION);
                            }
                        }
                        break;
                    case MessageParameter.PEDESTRIAN:
                        switch (rcv.getPerformative()) {
                            case ACLMessage.REQUEST_WHEN:
                                int stationId =
                                        Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.STATION_ID));
                                print("Passenger " + rcv.getSender().getLocalName() + " entered the bus.");
                                bus.addPassengerToStation(stationId, rcv.getSender().getLocalName());

                                ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                                response.addReceiver(rcv.getSender());

                                Properties properties = new Properties();
                                properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
                                response.setAllUserDefinedParameters(properties);
                                send(response);
                                print("Passengers: " + bus.getPassengersCount());
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

    private void getNextStation() {
        // finds next station and announces his arrival
        var stationOpt = bus.findNextStation();
        if (stationOpt.isPresent()) {
            var station = stationOpt.get();
            AID dest = new AID("Station" + station.getStationId(), AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(dest);
            Properties properties = new Properties();
            Instant currentTime = MasterAgent.getSimulationTime().toInstant();
            Instant time = currentTime.plusMillis(bus.getMillisecondsToNextStation());
            properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
            properties.setProperty(MessageParameter.BUS_LINE, "" + bus.getLine());

            var timeOnStation = bus.getTimeOnStation(station.getOsmStationId());
            timeOnStation.ifPresent(date -> properties.setProperty(MessageParameter.SCHEDULE_ARRIVAL, "" + date.toInstant()));

            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + time);
            msg.setAllUserDefinedParameters(properties);

            send(msg);
            print("Sending INFORM to Station" + station.getStationId() + ".");
        }
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
            logger.debug("Agent " + getName() + " not in initiated state. State: " + this.getAgentState().getName());
            return;
        }

        var date = timeProvider.getCurrentSimulationTime();
        long hours = bus.getBoardingTime().getHours();
        long minutes = bus.getBoardingTime().getMinutes();
        if (hours == date.getHours() && minutes == date.getMinutes()) {
            logger.debug("Running! (" + hours + ":" + minutes+ ")");
            start();
        }
    }
}
