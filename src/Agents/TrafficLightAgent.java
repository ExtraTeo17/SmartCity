package Agents;

import LightStrategy.BasicLightStrategy;
import LightStrategy.LightStrategy;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightAgent extends Agent {
    public LightColor lightColor = LightColor.RED;
    public List<String> queue = new ArrayList<>();
    LightStrategy strategy = new BasicLightStrategy();

    protected void setup() {
        System.out.println("I'm a light!");
        System.out.println("Red light.");
        strategy.ApplyStrategy(this);
    }

    protected void takeDown() {
        super.takeDown();
    }
}
