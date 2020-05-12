package Vehicles;

import Routing.RouteNode;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class TestCar extends RegularCar {
    Instant start;

    public TestCar(List<RouteNode> info) {
        super(info);
    }

    @Override
    public void setState(DrivingState state) {
        if (getState() == DrivingState.STARTING)
            start = Instant.now();
        super.setState(state);
        if(state == DrivingState.AT_DESTINATION)
        {
            Instant end = Instant.now();
            System.out.println("Test finished in time: " + Duration.between(start, end));
        }
    }
}
