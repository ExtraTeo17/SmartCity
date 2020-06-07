package Agents;

import java.time.Instant;
import java.util.List;

import Routing.LightManagerNode;
import Routing.RouteNode;
import Routing.StationNode;
import SmartCity.Timetable;
import Vehicles.DrivingState;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;

public class BusAgent extends Agent {

    private final long agentId;
    private Bus bus;

    public BusAgent(final List<RouteNode> route, final Timetable timetable, final int busId) {
        agentId = busId;
        bus = new Bus(route, timetable);

        StationNode station = bus.getCurrentStationNode();
        Print("Started at station " + station.getStationId() + ".");
        bus.setState(DrivingState.WAITING_AT_STATION);
        // send info to correct passengers to leave
        // set stuff regarding waiting for passengers (communicate with station)

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
                            Print("Passing");
                            bus.Move();
                            bus.setState(DrivingState.MOVING);
                            break;
                    }
                } else if (bus.isAtStation()) {
                    switch (bus.getState()) {
                        case MOVING:
                            StationNode station = bus.getCurrentStationNode();
                            Print("Arrived at station " + station.getStationId() + ".");
                            bus.setState(DrivingState.WAITING_AT_STATION);
                            // send info to correct passengers to leave
                            // set stuff regarding waiting for passengers (communicate with station)
                            break;
                        case WAITING_AT_STATION:
                            // waiting for passengers...

                            // this should be set by communication with station
                            bus.setState(DrivingState.PASSING_STATION);
                            break;
                        case PASSING_STATION:
                            RouteNode node = bus.findNextStop();
                            if(node instanceof LightManagerNode)
                            {
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
                                if(bus.findNextStop() instanceof LightManagerNode) GetNextLight();
                                bus.setState(DrivingState.PASSING_LIGHT);
                            }
                            else if(bus.getState() == DrivingState.WAITING_AT_STATION){
                            	GetNextStation();
                            	bus.setState(DrivingState.PASSING_STATION);
							}
                            break;
                    }
                }
                block(100);
            }
        };

        addBehaviour(move);
        addBehaviour(communication);
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

    void Print(String message) {
        System.out.println(getLocalName() + ": " + message);
    }

}
