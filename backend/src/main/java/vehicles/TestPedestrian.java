package vehicles;

import routing.RouteNode;
import routing.StationNode;
import smartcity.MasterAgent;

import java.time.Instant;
import java.util.List;

public class TestPedestrian extends Pedestrian {
    public Instant start;
    public Instant end;

    public TestPedestrian(List<RouteNode> routeToStation, List<RouteNode> routeFromStation, String preferredBusLine,
                          StationNode startStation, StationNode finishStation) {
        super(routeToStation, routeFromStation, preferredBusLine, startStation, finishStation);
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
