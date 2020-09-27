package vehicles;

import routing.RouteNode;
import routing.StationNode;
import smartcity.ITimeProvider;

import java.time.LocalDateTime;
import java.util.List;

public class TestPedestrian extends Pedestrian {
    private final ITimeProvider timeProvider;

    public LocalDateTime start;
    public LocalDateTime end;

    public TestPedestrian(List<RouteNode> routeToStation,
                          List<RouteNode> routeFromStation,
                          String preferredBusLine,
                          StationNode startStation,
                          StationNode finishStation,
                          ITimeProvider timeProvider) {
        super(routeToStation, routeFromStation, preferredBusLine, startStation, finishStation);
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
