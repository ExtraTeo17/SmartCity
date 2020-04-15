package Agents;

import LightStrategies.BasicLightStrategy;
import LightStrategies.LightStrategy;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.List;

public class TrafficLightAgent extends Agent {
    public LightColor lightColor = LightColor.RED;
    public List<String> queue = new ArrayList<>();
    LightStrategy strategy = new BasicLightStrategy();

    protected void setup() {
        Print("I'm a traffic light.");
        Print("Red light.");
       // strategy.ApplyStrategy(this); kiedy strategy będzie gotowa, to odkomentować
    }

    protected void takeDown() {
        super.takeDown();
    }

    public void Print(String message){
        System.out.println(getLocalName() + ": " + message);
    }
}
