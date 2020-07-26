package vehicles;

import routing.RouteNode;
import smartcity.MasterAgent;

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
            start = MasterAgent.getSimulationTime().toInstant();
        }
        super.setState(state);
        if (state == DrivingState.AT_DESTINATION) {
            end = MasterAgent.getSimulationTime().toInstant();
        }
    }
}
