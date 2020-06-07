package Agents;

import java.util.List;

import Routing.RouteNode;
import Vehicles.Pedestrian;
import jade.core.Agent;

public class PedestrianAgent extends Agent {

	private final long agentId;
	private final Pedestrian pedestrian;
	
	public PedestrianAgent(final Pedestrian pedestrian, final int agentId) {
		this.pedestrian = pedestrian;
		this.agentId = agentId;
	}
	
	public Pedestrian getPedestrian() {
		return pedestrian;
	}
	
	public String getAgentId() {
		return Long.toString(agentId);
	}
}
