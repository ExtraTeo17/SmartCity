package vehicles;

import com.google.common.annotations.VisibleForTesting;
import routing.LightManagerNode;
import routing.RouteNode;
import routing.RoutingConstants;
import routing.StationNode;

import java.util.ArrayList;
import java.util.List;

public class Pedestrian extends MovingObject {
    private final String preferredBusLine;
    private final List<RouteNode> displayRouteBeforeBus;
    private final List<RouteNode> displayRouteAfterBus;
    private final List<RouteNode> routeBeforeBus;
    private final StationNode stationStart;
    private final StationNode stationFinish;

    private transient int stationIndex = 0;

    public Pedestrian(List<RouteNode> routeToStation,
                      List<RouteNode> uniformRouteToStation,
                      List<RouteNode> routeFromStation,
                      List<RouteNode> uniformRouteFromStation,
                      String preferredBusLine,
                      StationNode startStation,
                      StationNode finishStation) {
        super(10, createRoute(startStation, uniformRouteToStation, finishStation, uniformRouteFromStation));
        this.displayRouteBeforeBus = routeToStation;
        this.routeBeforeBus = uniformRouteToStation;
        this.routeBeforeBus.add(startStation);

        this.displayRouteAfterBus = routeFromStation;
        this.stationIndex = routeBeforeBus.size() - 1;
        this.preferredBusLine = preferredBusLine;

        this.stationStart = startStation;
        this.stationFinish = finishStation;
    }

    private static List<RouteNode> createRoute(StationNode startStation,
                                               List<RouteNode> uniformRouteToStation,
                                               StationNode finishStation,
                                               List<RouteNode> uniformRouteFromStation) {
        var route = new ArrayList<>(uniformRouteToStation);
        route.add(startStation);
        route.add(finishStation);
        route.addAll(uniformRouteFromStation);

        return route;
    }

    Pedestrian(Pedestrian ped) {
        super(ped.speed, ped.route);
        this.displayRouteBeforeBus = ped.displayRouteBeforeBus;
        this.routeBeforeBus = ped.routeBeforeBus;

        this.displayRouteAfterBus = ped.displayRouteAfterBus;
        this.stationIndex = ped.stationIndex;
        this.preferredBusLine = ped.preferredBusLine;

        this.stationStart = ped.stationStart;
        this.stationFinish = ped.stationFinish;
    }

    @VisibleForTesting
    Pedestrian() {
        super(10, new ArrayList<>());
        preferredBusLine = "";
        displayRouteBeforeBus = new ArrayList<>();
        displayRouteAfterBus = new ArrayList<>();
        routeBeforeBus = new ArrayList<>();
        stationStart = new StationNode(5, 5, 1L, 1);
        stationFinish = new StationNode(5, 10, 2L, 2);
    }

    public StationNode getStartingStation() {
        return (StationNode) route.get(stationIndex);
    }

    public StationNode getTargetStation() {
        return (StationNode) route.get(stationIndex + 1);
    }

    public String getPreferredBusLine() {
        return preferredBusLine;
    }

    @Override
    public long getAdjacentOsmWayId() {
        return ((LightManagerNode) route.get(moveIndex)).getCrossingOsmId1();
    }

    @Override
    public String getVehicleType() {
        return VehicleType.PEDESTRIAN.toString();
    }

    public RouteNode findNextStop() {
        for (int i = moveIndex + 1; i < route.size(); i++) {
            if (route.get(i) instanceof StationNode) {
                return route.get(i);
            }
            if (route.get(i) instanceof LightManagerNode) {
                return route.get(i);
            }
        }
        return null;
    }

    public boolean isAtStation() {
        if (moveIndex == route.size()) {
            return false;
        }
        return route.get(moveIndex) instanceof StationNode;
    }

    @Override
    public List<RouteNode> getDisplayRoute() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMillisecondsToNextLight() {
        return ((closestLightIndex - moveIndex) * RoutingConstants.STEP_CONSTANT) / getSpeed();
    }

    public List<RouteNode> getDisplayRouteBeforeBus() {
        return displayRouteBeforeBus;
    }

    public List<RouteNode> getDisplayRouteAfterBus() {
        return displayRouteAfterBus;
    }

    public int getMillisecondsToNextStation() {
        return ((routeBeforeBus.size() - 1 - moveIndex) * RoutingConstants.STEP_CONSTANT) / getSpeed();

    }

    public StationNode findNextStation() {
        return stationStart;
    }
}
