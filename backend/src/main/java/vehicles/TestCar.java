package vehicles;

import routing.RouteNode;
import smartcity.ITimeProvider;

import java.time.LocalDateTime;
import java.util.List;

public class TestCar extends MovingObjectImpl {
    private final ITimeProvider timeProvider;

    private LocalDateTime start;
    private LocalDateTime end;

    public TestCar(List<RouteNode> route,
                   List<RouteNode> uniformRoute,
                   ITimeProvider timeProvider) {
        super(route, uniformRoute);
        this.timeProvider = timeProvider;
    }

    @Override
    public void setState(DrivingState newState) {
        var initialState = getState();
        if (initialState == DrivingState.STARTING) {
            start = timeProvider.getCurrentSimulationTime();
        }
        if (newState == DrivingState.AT_DESTINATION) {
            end = timeProvider.getCurrentSimulationTime();
            ;
        }

        super.setState(newState);
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
