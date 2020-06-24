package Agents;

import Routing.LightManagerNode;
import Routing.RouteNode;
import Routing.StationNode;
import SmartCity.SmartCityAgent;
import Vehicles.DrivingState;
import Vehicles.Pedestrian;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;

import java.time.Instant;
import java.util.List;

public class PedestrianAgent extends Agent {

	private final long agentId;
	private final Pedestrian pedestrian;

	public boolean isInBus() { return DrivingState.IN_BUS == pedestrian.getState();}
	
	public PedestrianAgent(final Pedestrian pedestrian, final int agentId) {
		this.pedestrian = pedestrian;
		this.agentId = agentId;
		GetNextStation();
	
	
	} 
	@Override
    protected void setup() {
		pedestrian.setState(DrivingState.MOVING);
		Behaviour move = new TickerBehaviour(this, 3600 / pedestrian.getSpeed()) {
			@Override
			public void onTick() {
				if (pedestrian.isAtTrafficLights()) {
					switch (pedestrian.getState()) {
					case MOVING:
						LightManagerNode light = pedestrian.getCurrentTrafficLightNode();
						ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
						msg.addReceiver(new AID("LightManager" + light.getLightManagerId(), AID.ISLOCALNAME));
						Properties properties = new Properties();
						properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
						properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(pedestrian.getAdjacentOsmWayId()));
						msg.setAllUserDefinedParameters(properties);
						send(msg);
						pedestrian.setState(DrivingState.WAITING_AT_LIGHT);
						Print("Asking LightManager" + light.getLightManagerId() + " for right to passage.");
						break;
						
						case WAITING_AT_LIGHT:

							break;
						case PASSING_LIGHT:
							Print("Passing the light.");
							pedestrian.Move();
							pedestrian.setState(DrivingState.MOVING);
							break;
					}
				} else if (pedestrian.isAtStation()) {
					switch (pedestrian.getState()) {
						case MOVING:
							StationNode station = pedestrian.getStartingStation();

							ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
							msg.addReceiver(new AID("Station" + station.getStationId(), AID.ISLOCALNAME));
							Properties properties = new Properties();
							properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
							properties.setProperty(MessageParameter.DESIRED_BUS,pedestrian.getPreferredBusLine());
							properties.setProperty(MessageParameter.ARRIVAL_TIME,"" + SmartCityAgent.getSimulationTime().toInstant());
							msg.setAllUserDefinedParameters(properties);
							send(msg);
							   System.out.println("Pedestrian: Send REQUEST_WHEN to Station");
							
							pedestrian.setState(DrivingState.WAITING_AT_STATION);
							break;
						case WAITING_AT_STATION:
							// waiting for bus...

							break;
						case IN_BUS:
							// traveling inside a bus

							break;
						case PASSING_STATION:
						
							pedestrian.Move();
							pedestrian.setState(DrivingState.MOVING);
							break;
					}
				} else if(pedestrian.isAtDestination())
				{
					pedestrian.setState(DrivingState.AT_DESTINATION);
					Print("Reached destination.");

					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.addReceiver(new AID("SmartCityAgent", AID.ISLOCALNAME));
					Properties prop = new Properties();
					prop.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
					prop.setProperty(MessageParameter.AT_DESTINATION, String.valueOf(Boolean.TRUE));
					msg.setAllUserDefinedParameters(prop);
					send(msg);
					doDelete();
				}
				else {
					pedestrian.Move();
				}
			}
		};

		Behaviour communication = new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage rcv = receive();
				if (rcv != null) {
					String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
					if(type == MessageParameter.LIGHT)
					{
						switch (rcv.getPerformative()) {
							case ACLMessage.REQUEST:
								if (pedestrian.getState() == DrivingState.WAITING_AT_LIGHT) {
									ACLMessage response = new ACLMessage(ACLMessage.AGREE);
									response.addReceiver(rcv.getSender());
									Properties properties = new Properties();

									properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
									properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(pedestrian.getAdjacentOsmWayId()));
									response.setAllUserDefinedParameters(properties);
									send(response);
									if (pedestrian.findNextStop() instanceof LightManagerNode) GetNextLight();
									pedestrian.setState(DrivingState.PASSING_LIGHT);
								}
								break;
						}
					}
					else if(type == MessageParameter.STATION)
					{
						switch (rcv.getPerformative()) {
							case ACLMessage.REQUEST:
								ACLMessage response = new ACLMessage(ACLMessage.AGREE);
								response.addReceiver(rcv.getSender());
								Properties properties = new Properties();

								properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
								response.setAllUserDefinedParameters(properties);
								send(response);

								ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
								String busAgentId =rcv.getUserDefinedParameter(MessageParameter.BUS_ID);
								msg.addReceiver(new AID(busAgentId, AID.ISLOCALNAME));
								properties = new Properties();
								properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
								properties.setProperty(MessageParameter.STATION_ID, "" + pedestrian.getTargetStation().getStationId());
								msg.setAllUserDefinedParameters(properties);
								pedestrian.setState(DrivingState.IN_BUS);
								send(msg);
								break;
						}
					} else if(type == MessageParameter.BUS)
					{
						switch (rcv.getPerformative()) {
							case ACLMessage.REQUEST:
								ACLMessage response = new ACLMessage(ACLMessage.AGREE);
								response.addReceiver(rcv.getSender());
								Properties properties = new Properties();

								properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
								properties.setProperty(MessageParameter.STATION_ID,""+pedestrian.getTargetStation().getStationId());
								response.setAllUserDefinedParameters(properties);
								send(response);
								pedestrian.Move();
								pedestrian.setState(DrivingState.PASSING_STATION);

								GetNextLight();

								break;
						}
					}
				}
			}
		};

		addBehaviour(move);
		addBehaviour(communication);
	}
	 void GetNextStation() {
	        // finds next station and announces his arrival
	        StationNode nextStation = pedestrian.findNextStation();
	    	pedestrian.setState(DrivingState.MOVING);
	        if (nextStation != null) {
	        	System.out.println("Pedestrian: send INFORM to station");
	            AID dest = new AID("Station" + nextStation.getStationId(), AID.ISLOCALNAME);
	            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	            msg.addReceiver(dest);
	            Properties properties = new Properties();
	            Instant currentTime = SmartCityAgent.getSimulationTime().toInstant();
	            Instant time =currentTime.plusMillis(pedestrian.getMilisecondsToNextStation());
	            properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
	            properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + time);
	            properties.setProperty(MessageParameter.DESIRED_BUS, "" + pedestrian.getPreferredBusLine());
	            msg.setAllUserDefinedParameters(properties);

	            send(msg);
	            System.out.println("Pedestrian: Send Inform to Station");
	        }
	    }

	void GetNextLight() {
		// finds next traffic light and announces his arrival
		LightManagerNode nextManager = pedestrian.findNextTrafficLight();

		if (nextManager != null) {

			AID dest = new AID("LightManager" + nextManager.getLightManagerId(), AID.ISLOCALNAME);
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(dest);
			Properties properties = new Properties();
			Instant time = SmartCityAgent.getSimulationTime().toInstant().plusMillis(pedestrian.getMilisecondsToNextLight());
			properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);
			properties.setProperty(MessageParameter.ARRIVAL_TIME, "" + time);
			properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, "" + nextManager.getOsmWayId());
			msg.setAllUserDefinedParameters(properties);

			send(msg);
			Print("Sending INFORM to LightManager" + nextManager.getLightManagerId() + ".");
		}
	}

	public Pedestrian getPedestrian() {
		return pedestrian;
	}
	
	public String getAgentId() {
		return Long.toString(agentId);
	}

	void Print(String message) {
		System.out.println(getLocalName() + ": " + message);
	}
}
