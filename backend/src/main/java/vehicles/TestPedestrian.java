package vehicles;

import com.google.common.annotations.VisibleForTesting;
import routing.RouteNode;
import routing.StationNode;
import smartcity.ITimeProvider;

import java.time.LocalDateTime;
import java.util.List;

public class TestPedestrian extends Pedestrian {
    private final ITimeProvider timeProvider;

    private LocalDateTime start;
    private LocalDateTime end;

    @VisibleForTesting
    TestPedestrian(ITimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public TestPedestrian(List<RouteNode> routeToStation,
                          List<RouteNode> uniformRouteToStation,
                          List<RouteNode> routeFromStation,
                          List<RouteNode> uniformRouteFromStation,
                          String preferredBusLine,
                          StationNode startStation,
                          StationNode finishStation,
                          ITimeProvider timeProvider) {
        super(routeToStation, uniformRouteToStation, routeFromStation, uniformRouteFromStation,
                preferredBusLine, startStation, finishStation);
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
