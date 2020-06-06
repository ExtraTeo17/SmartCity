package Agents;

import java.util.List;

import Routing.RouteNode;
import Vehicles.Pedestrian;

public class PedestrianAgent {

	private final long agentId;
	private final Pedestrian pedestrian;
	
	public PedestrianAgent(final List<RouteNode> route, final int agentId) {
		this.agentId = agentId;
		pedestrian = new Pedestrian(route);
	}
	
	public Pedestrian getPedestrian() {
		return pedestrian;
	}
	
	public String getId() {
		return Long.toString(agentId);
	}
}
