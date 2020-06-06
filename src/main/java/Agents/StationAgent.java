package Agents;

import jade.core.Agent;

import org.jxmapviewer.viewer.GeoPosition;

import SmartCity.StationOSMNode;

public class StationAgent extends Agent {
	
	private final StationOSMNode stationOSMNode;
	private final long agentId;
	
	public StationAgent(final StationOSMNode stationOSMNode, final long agentId) { // REMEMBER TO PRUNE BEYOND CIRCLE
		this.stationOSMNode = stationOSMNode;
		this.agentId = agentId;
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
