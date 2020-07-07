package Agents;

import Routing.LightManagerNode;
import Routing.RouteNode;
import Routing.StationNode;
import SmartCity.Buses.Timetable;
import SmartCity.SmartCityAgent;
import Vehicles.Bus;
import Vehicles.DrivingState;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.javatuples.Pair;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Random;

@SuppressWarnings("serial")
public class BusAgent extends Agent {

    private long agentId;
    private Bus bus;

    @Override
    protected void setup() {
        GetNextStation();
        StationNode station = bus.getCurrentStationNode();
        Print("Started at station " + station.getStationId() + ".");
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
                            Print("Asking LightManager" + light.getLightManagerId() + " for right to passage.");

                            break;
                        case WAITING_AT_LIGHT:

                            break;
                        case PASSING_LIGHT:
                            Print("Passing the light.");
                            bus.Move();
                            bus.setState(DrivingState.MOVING);
                            break;
                    }
                }
                else if (bus.isAtDestination()) {
                    bus.setState(DrivingState.AT_DESTINATION);
                    Print("Reached destination.");

                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID("SmartCityAgent", AID.ISLOCALNAME));
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
                            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + SmartCityAgent.getSimulationTime().toInstant());
                            System.out.println("BUS: send REQUEST_WHEN to station");
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);

                            Print("Arrived at station " + station.getStationId() + ".");
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
                                GetNextLight();
                            }
                            GetNextStation();

                            bus.setState(DrivingState.MOVING);
                            bus.Move();
                            break;
                    }
                }
                else {
                    bus.Move();
                }
            }

            private void sendAgreeMsg() {
                StationNode station = bus.getCurrentStationNode();

                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
                msg.addReceiver(new AID("Station" + station.getStationId(), AID.ISLOCALNAME));
                Properties properties = new Properties();
                properties = new Properties();
                properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
            }
        };


        Behaviour communication = new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv != null) {
                    String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                    if (type == MessageParameter.LIGHT) {
                        switch (rcv.getPerformative()) {
                            case ACLMessage.REQUEST:
                                if (bus.getState() == DrivingState.WAITING_AT_LIGHT) {
                                    ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                                    response.addReceiver(rcv.getSender());
                                    Properties properties = new Properties();

                                    properties.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
                                    properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(bus.getAdjacentOsmWayId()));
                                    response.setAllUserDefinedParameters(properties);
                                    send(response);
                                    if (bus.findNextStop() instanceof LightManagerNode) {
                                        GetNextLight();
                                    }
                                    bus.setState(DrivingState.PASSING_LIGHT);
                                }
                                break;
                        }
                    }
                    else if (type == MessageParameter.STATION) {
                        switch (rcv.getPerformative()) {
                            case ACLMessage.REQUEST:
                                if (bus.getState() == DrivingState.WAITING_AT_STATION) {
                                    ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                                    response.addReceiver(rcv.getSender());

                                    Properties properties = new Properties();
                                    properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
                                    response.setAllUserDefinedParameters(properties);
                                    send(response);
                                    System.out.println("BUS: get REQUEST from station");
                                    GetNextStation();
                                    bus.setState(DrivingState.PASSING_STATION);
                                }
                                break;
                        }
                    }
                    else if (type == MessageParameter.PEDESTRIAN) {
                        switch (rcv.getPerformative()) {
                            case ACLMessage.REQUEST_WHEN:

                                Long stationId = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.STATION_ID));
                                Print("Passenger " + rcv.getSender().getLocalName() + " entered the bus.");
                                bus.addPassengerToStation(stationId, rcv.getSender().getLocalName());

                                ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                                response.addReceiver(rcv.getSender());

                                Properties properties = new Properties();
                                properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
                                response.setAllUserDefinedParameters(properties);
                                send(response);
                                Print("Passengers: " + bus.getPassengersCount());
                                break;
                            case ACLMessage.AGREE:
                                stationId = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.STATION_ID));
                                Print("Passenger " + rcv.getSender().getLocalName() + " left the bus.");
                                bus.removePassengerFromStation(stationId, rcv.getSender().getLocalName());
                                Print("Passengers: " + bus.getPassengersCount());
                                break;
                        }

                    }
                }
                block(100);
            }
        };

        addBehaviour(move);
        addBehaviour(communication);
    }

    @SuppressWarnings("unchecked")
    private void readArgumentsAndCreateBus() {
        final Object[] args = getArguments();
        bus = new Bus((List<RouteNode>) args[0], (Timetable) args[1],
                (String) args[2], (String) args[3]);
        agentId = (int) args[4];
    }

    void GetNextLight() {
        // finds next traffic light and announces his arrival
        LightManagerNode nextManager = bus.findNextTrafficLight();

        if (nextManager != null) {

            AID dest = new AID("LightManager" + nextManager.getLightManagerId(), AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(dest);
            Properties properties = new Properties();
            Instant time = SmartCityAgent.getSimulationTime().toInstant().plusMillis(bus.getMilisecondsToNextLight());
            properties.setProperty(MessageParameter.TYPE, MessageParameter.VEHICLE);
            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + time);
            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, "" + nextManager.getOsmWayId());
            msg.setAllUserDefinedParameters(properties);

            send(msg);
            Print("Sending INFORM to LightManager" + nextManager.getLightManagerId() + ".");
        }
    }

    void GetNextStation() {
        // finds next station and announces his arrival
        StationNode nextStation = bus.findNextStation();

        if (nextStation != null) {
            AID dest = new AID("Station" + nextStation.getStationId(), AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(dest);
            Properties properties = new Properties();
            Instant currentTime = SmartCityAgent.getSimulationTime().toInstant();
            Instant time = currentTime.plusMillis(bus.getMilisecondsToNextStation());
            properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
            properties.setProperty(MessageParameter.BUS_LINE, "" + bus.getLine());
            properties.setProperty(MessageParameter.SCHEDULE_ARRIVAL, "" + bus.getTimeOnStation(nextStation.getOsmStationId()).toInstant());
            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + time);
            msg.setAllUserDefinedParameters(properties);

            send(msg);
            Print("Sending INFORM to Station" + nextStation.getStationId() + ".");
        }
    }

    public Bus getBus() {
        return bus;
    }


    public String getId() {
        return Long.toString(agentId);
    }

    public final String getLine() {
        return bus.getLine();
    }

    public final Pair<StationNode, StationNode> getTwoSubsequentStations(final Random random) { // TODO: Fix situation where bus route contains only one station and pedestrians tries to choose two
        List<StationNode> stationsOnRoute = bus.getStationNodesOnRoute();
        final int halfIndex = stationsOnRoute.size() / 2;
        return Pair.with(stationsOnRoute.get(random.nextInt(halfIndex)),
                stationsOnRoute.get(halfIndex + random.nextInt(halfIndex)));
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }

    void Print(String message) {
        System.out.println(getLocalName() + ": " + message);
    }


    public void runBasedOnTimetable(Date date) {
        if (this.getAgentState().getValue() != jade.wrapper.AgentState.cAGENT_STATE_INITIATED) {
            return;
        }
        readArgumentsAndCreateBus();

        long hours = bus.getBoardingTime().getHours();
        long minutes = bus.getBoardingTime().getMinutes();
        if (hours == date.getHours() && minutes == date.getMinutes()) {
            SmartCityAgent.ActivateAgent(this);
        }
    }
}
