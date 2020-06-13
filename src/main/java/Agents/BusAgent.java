package Agents;

import java.time.Instant;
import java.util.List;
import java.util.Random;

import Routing.LightManagerNode;
import Routing.RouteNode;
import Routing.StationNode;
import SmartCity.Buses.Timetable;
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

@SuppressWarnings("serial")
public class BusAgent extends Agent {

	private long agentId;
    private Bus bus;

    @Override
	protected void setup() {
    	readArgumentsAndCreateBus();

        StationNode station = bus.getCurrentStationNode();
        Print("Started at station " + station.getStationId() + ".");

        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
        msg.addReceiver(new AID("Station" + station.getStationId(), AID.ISLOCALNAME));
        Properties properties = new Properties();
        properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
        msg.setAllUserDefinedParameters(properties);
        send(msg);

        bus.setState(DrivingState.WAITING_AT_STATION);

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
                } else if (bus.isAtStation()) {
                    switch (bus.getState()) {
                        case MOVING:

                            // TODO Send info to correct passengers to leave

                            StationNode station = bus.getCurrentStationNode();
                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
                            msg.addReceiver(new AID("Station" + station.getStationId(), AID.ISLOCALNAME));
                            Properties properties = new Properties();
                            properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);

                            Print("Arrived at station " + station.getStationId() + ".");
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
                                GetNextLight();
                            }
                            GetNextStation();

                            bus.setState(DrivingState.MOVING);
                            bus.Move();
                            break;
                    }
                } else {
                    bus.Move();
                }
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
                                    if (bus.findNextStop() instanceof LightManagerNode) GetNextLight();
                                    bus.setState(DrivingState.PASSING_LIGHT);
                                }
                                break;
                        }
                    } else if (type == MessageParameter.STATION) {
                        switch (rcv.getPerformative()) {
                            case ACLMessage.REQUEST:
                                if (bus.getState() == DrivingState.WAITING_AT_STATION) {
                                    ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                                    response.addReceiver(rcv.getSender());

                                    Properties properties = new Properties();
                                    properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
                                    response.setAllUserDefinedParameters(properties);
                                    send(response);

                                    GetNextStation();
                                    bus.setState(DrivingState.PASSING_STATION);
                                }
                                break;
                        }
                    } else if (type == MessageParameter.PEDESTRIAN) {
                        switch (rcv.getPerformative())
                        {
                            case ACLMessage.REQUEST_WHEN:
                                // TODO Add passenger to list (needed to ask him to leave when at destination)
                                //  use MessageParameter.STATION_ID

                                ACLMessage response = new ACLMessage(ACLMessage.AGREE);
                                response.addReceiver(rcv.getSender());

                                Properties properties = new Properties();
                                properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
                                response.setAllUserDefinedParameters(properties);
                                send(response);
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
        bus = new Bus((List<RouteNode>)args[0], (Timetable)args[1],
        		(String)args[2], (String)args[3]);
    	agentId = (int)args[4];
	}

    void GetNextLight() {
        // finds next traffic light and announces his arrival
        LightManagerNode nextManager = bus.findNextTrafficLight();

        if (nextManager != null) {

            AID dest = new AID("LightManager" + nextManager.getLightManagerId(), AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(dest);
            Properties properties = new Properties();
            Instant time = Instant.now().plusMillis(bus.getMilisecondsToNextLight());
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
            Instant time = Instant.now().plusMillis(bus.getMilisecondsToNextStation());
            properties.setProperty(MessageParameter.TYPE, MessageParameter.BUS);
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

    public final Pair<StationNode, StationNode> getTwoSubsequentStations(final Random random) {
        List<StationNode> stationsOnRoute = bus.getStationNodesOnRoute();
        final int halfIndex = stationsOnRoute.size() / 2;
        return Pair.with(stationsOnRoute.get(random.nextInt(halfIndex)),
                stationsOnRoute.get(halfIndex + random.nextInt(halfIndex) - 1));
    }

	@Override
	protected void takeDown() {
		super.takeDown();
	}

    void Print(String message) {
        System.out.println(getLocalName() + ": " + message);
    }
}
