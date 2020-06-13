package Agents;

import Vehicles.Pedestrian;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;

public class PedestrianAgent extends Agent {

	private final long agentId;
	private final Pedestrian pedestrian;
	
	public PedestrianAgent(final Pedestrian pedestrian, final int agentId) {
		this.pedestrian = pedestrian;
		this.agentId = agentId;

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
								// TODO Pedestrian is asked to pass the light
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
								Long busId = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.BUS_ID));
								msg.addReceiver(new AID("Bus" + busId, AID.ISLOCALNAME));
								properties = new Properties();
								properties.setProperty(MessageParameter.TYPE, MessageParameter.PEDESTRIAN);

								// TODO Set desired station to leave on (use MessageParameter.STATION_ID)

								msg.setAllUserDefinedParameters(properties);
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
								response.setAllUserDefinedParameters(properties);
								send(response);

								// TODO Passenger was asked to leave the bus

								break;
						}
					}
				}
			}
		};

		addBehaviour(communication);
	}
	
	public Pedestrian getPedestrian() {
		return pedestrian;
	}
	
	public String getAgentId() {
		return Long.toString(agentId);
	}
}
