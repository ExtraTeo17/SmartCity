package Agents;

import SmartCity.BusInfo;
import jade.core.Agent;

public class BusAgent extends VehicleAgent {

    private final long agentId;
    private final Bus bus;
    
	public BusAgent(BusInfo info, int busId) {
		agentId = busId;
		bus = new Bus(null);
	}

	public String getId() {
		return Long.toString(agentId);
	}
}
