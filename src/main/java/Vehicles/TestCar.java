package Vehicles;

import Agents.MessageParameter;
import Routing.RouteNode;
import SmartCity.SmartCityAgent;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class TestCar extends MovingObjectImpl {
    public Instant start;
    public Instant end;

    public TestCar(List<RouteNode> info) {
        super(info);
    }

    @Override
    public void setState(DrivingState state) {
        if (getState() == DrivingState.STARTING)
            start = SmartCityAgent.getSimulationTime().toInstant();
        super.setState(state);
        if(state == DrivingState.AT_DESTINATION)
        {
            end = SmartCityAgent.getSimulationTime().toInstant();
        }
    }
}
