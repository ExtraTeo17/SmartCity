package Agents;


import LightStrategies.LightManagerStrategy;
import LightStrategies.LightStrategy;
import jade.core.Agent;

public class LightManager extends Agent {
    LightStrategy strategy = new LightManagerStrategy();
    protected void setup() {
        Print("I'm a traffic manager.");

        strategy.ApplyStrategy(this);
    }

    public void takeDown() {
        super.takeDown();
    }

    public void Print(String message) {
        System.out.println(getLocalName() + ": " + message);
    }
}
