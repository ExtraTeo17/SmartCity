package Agents;

import jade.core.Agent;

import org.jxmapviewer.viewer.GeoPosition;

import OSMProxy.Elements.OSMStation;
import SmartCity.Stations.StationStrategy;

public class StationAgent extends Agent {
	private final StationStrategy stationStrategy;
	//private final OSMStation stationOSMNode;
	private final long agentId;
	
	public StationAgent( OSMStation stationOSMNode, final long agentId) { // REMEMBER TO PRUNE BEYOND CIRCLE
		this.stationStrategy = new StationStrategy(stationOSMNode,agentId);	
		
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

	//public GeoPosition getPosition() {
	//	return stationOSMNode.getPosition();
//	}
}
