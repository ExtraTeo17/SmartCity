package Vehicles;

import Routing.RouteNode;
import SmartCity.SmartCityAgent;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class TestCar extends MovingObjectImpl {
    Instant start;

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
            Instant end = SmartCityAgent.getSimulationTime().toInstant();
            System.out.println("Test finished in time: " + Duration.between(start, end));
        }
    }
}
