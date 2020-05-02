package Agents;

import jade.core.Agent;
import SmartCity.Station;

public class StationAgent extends Agent {
	
	private Station station;
	
	protected void setup() {
		print("Hi! I'm a station agent!");
	}
	
	public void takeDown() {
		super.takeDown();
	}
	
    void print(String message){
        System.out.println(getLocalName() + ": " + message);
    }
}
