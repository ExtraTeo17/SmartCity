package vehicles;

import routing.RouteNode;
import smartcity.ITimeProvider;

import java.time.LocalDateTime;
import java.util.List;

public class TestCar extends MovingObjectImpl {
    private final ITimeProvider timeProvider;

    public LocalDateTime start;
    public LocalDateTime end;

    public TestCar(List<RouteNode> info, ITimeProvider timeProvider) {
        super(info);
        this.timeProvider = timeProvider;
    }

    @Override
    public void setState(DrivingState state) {
        super.setState(state);
        var currentTime = timeProvider.getCurrentSimulationTime();
        if (state == DrivingState.STARTING) {
            start = currentTime;
        }
        else if (state == DrivingState.AT_DESTINATION) {
            end = currentTime;
        }
    }
}
