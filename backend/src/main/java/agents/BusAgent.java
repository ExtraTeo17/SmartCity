package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.LightManagerNode;
import routing.RouteNode;
import routing.StationNode;
import smartcity.MasterAgent;
import vehicles.Bus;
import vehicles.DrivingState;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Random;

@SuppressWarnings("serial")
public class BusAgent extends AbstractAgent {
    private static final Logger logger = LoggerFactory.getLogger(BusAgent.class);
    private final Bus bus;

    public BusAgent(int busId, Bus bus) {
        super(busId);
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

        Behaviour move = new TickerBehaviour(this, 3600 / bus.getSpeed()) {
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

                            List<String> passengers = bus.getPassengersToLeave(station.getStationId());

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
                            properties.setProperty(MessageParameter.SCHEDULE_ARRIVAL, "" + bus.getTimeOnStation(station.getOsmStationId()).toInstant());
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
                                bus.removePassengerFromStation(stationId, rcv.getSender().getLocalName());
                                print("Passengers: " + bus.getPassengersCount());
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

    void getNextStation() {
        // finds next station and announces his arrival
        StationNode nextStation = bus.findNextStation();

        if (nextStation != null) {
            AID dest = new AID("Station" + nextStation.getStationId(), AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(dest);
            Properties properties = new Properties();
            Instant currentTime = MasterAgent.getSimulationTime().toInstant();
            Instant time = currentTime.plusMillis(bus.getMillisecondsToNextStation());
            properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
            properties.setProperty(MessageParameter.BUS_LINE, "" + bus.getLine());
            properties.setProperty(MessageParameter.SCHEDULE_ARRIVAL, "" + bus.getTimeOnStation(nextStation.getOsmStationId()).toInstant());
            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + time);
            msg.setAllUserDefinedParameters(properties);

            send(msg);
            print("Sending INFORM to Station" + nextStation.getStationId() + ".");
        }
    }

    public Bus getBus() {
        return bus;
    }

    public final String getLine() {
        return bus.getLine();
    }

    // TODO: Fix situation where bus route contains only one station and pedestrians tries to choose two
    public final Pair<StationNode, StationNode> getTwoSubsequentStations(final Random random) {
        List<StationNode> stationsOnRoute = bus.getStationNodesOnRoute();
        final int halfIndex = stationsOnRoute.size() / 2;
        return Pair.with(stationsOnRoute.get(random.nextInt(halfIndex)),
                stationsOnRoute.get(halfIndex + random.nextInt(halfIndex)));
    }

    // TODO: New bus was created each time - check if nothing was broken?
    public void runBasedOnTimetable(Date date) {
        if (this.getAgentState().getValue() != jade.wrapper.AgentState.cAGENT_STATE_INITIATED) {
            return;
        }

        long hours = bus.getBoardingTime().getHours();
        long minutes = bus.getBoardingTime().getMinutes();
        if (hours == date.getHours() && minutes == date.getMinutes()) {
            start();
        }
    }
}
