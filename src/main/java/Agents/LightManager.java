package Agents;

import LightStrategies.LightManagerStrategy;
import LightStrategies.LightStrategy;
import jade.core.Agent;

public class LightManager extends Agent {
	
    private final LightStrategy strategy = new LightManagerStrategy();
    
    protected void setup() {
        print("I'm a traffic manager.");
        strategy.ApplyStrategy(this);
    }

    public void takeDown() {
        super.takeDown();
    }

    public void print(String message) {
        System.out.println(getLocalName() + ": " + message);
    }
}
