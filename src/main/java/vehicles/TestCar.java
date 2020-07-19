package vehicles;

import routing.RouteNode;
import smartcity.SmartCityAgent;

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
        if (getState() == DrivingState.STARTING) {
            start = SmartCityAgent.getSimulationTime().toInstant();
        }
        super.setState(state);
        if (state == DrivingState.AT_DESTINATION) {
            end = SmartCityAgent.getSimulationTime().toInstant();
        }
    }
}
