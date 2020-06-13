package Agents;

import jade.core.Agent;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.jxmapviewer.viewer.GeoPosition;

import OSMProxy.Elements.OSMStation;

public class StationAgent extends Agent {
	
	private final OSMStation stationOSMNode;
	private final long agentId;
	
	public StationAgent(final OSMStation stationOSMNode, final long agentId) { // REMEMBER TO PRUNE BEYOND CIRCLE
		this.stationOSMNode = stationOSMNode;
		this.agentId = agentId;

		Behaviour communication = new CyclicBehaviour() {
			@Override
			public void action() {
				ACLMessage rcv = receive();
				if (rcv != null) {
					String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
					if (type == MessageParameter.BUS) {
						if (rcv.getPerformative() == ACLMessage.INFORM) {
							// TODO Handle inform from Bus (has anticipated time of arrival)

						} else if (rcv.getPerformative() == ACLMessage.REQUEST_WHEN) {
							// TODO Handle request_when from Bus (arrived and awaits request for departure)

							ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
							msg.addReceiver(rcv.getSender());
							send(msg);
						}
					} else if (type == MessageParameter.PEDESTRIAN) {
						if (rcv.getPerformative() == ACLMessage.INFORM) {
							// TODO Handle inform from Pedestrian (has anticipated time of arrival)

						} else if (rcv.getPerformative() == ACLMessage.REQUEST_WHEN) {
							// TODO Handle request_when from Pedestrian (arrived and awaits request for departure)
							//  use MessageParameter.STATION_ID

							ACLMessage msg = new ACLMessage(ACLMessage.AGREE);
							msg.addReceiver(rcv.getSender());
							send(msg);
						}
					}
				}
				block(100);
			}
		};

		addBehaviour(communication);
	}
	
	protected void setup() {
		print("Hi! I'm a station agent!");
	}
	
	public final long getAgentId() {
		return agentId;
	}
	
	public void takeDown() {
		super.takeDown();
	}
	
    void print(String message){
        System.out.println(getLocalName() + ": " + message);
    }

	public GeoPosition getPosition() {
		return stationOSMNode.getPosition();
	}
}
