package vehicles;

import com.google.common.annotations.VisibleForTesting;
import routing.RoutingConstants;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;
import smartcity.ITimeProvider;
import vehicles.enums.VehicleType;

import java.util.ArrayList;
import java.util.List;

import static vehicles.Constants.SPEED_SCALE;

public class Pedestrian extends MovingObject {
    private static final int DEFAULT_SPEED = 10 * SPEED_SCALE;

    private final String preferredBusLine;
    private final List<RouteNode> displayRouteBeforeBus;
    private final List<RouteNode> displayRouteAfterBus;
    private final List<RouteNode> routeBeforeBus;
    private final StationNode stationStart;
    private final StationNode stationFinish;

    private transient int stationIndex = 0;

    public Pedestrian(int agentId,
                      List<RouteNode> routeToStation,
                      List<RouteNode> uniformRouteToStation,
                      List<RouteNode> routeFromStation,
                      List<RouteNode> uniformRouteFromStation,
                      String preferredBusLine,
                      StationNode startStation,
                      StationNode finishStation,
                      ITimeProvider timeProvider) {
        super(timeProvider, agentId, DEFAULT_SPEED, createRoute(startStation, uniformRouteToStation,
                finishStation, uniformRouteFromStation));
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
        super(ped.timeProvider, ped.agentId, ped.speed, ped.uniformRoute);
        this.displayRouteBeforeBus = ped.displayRouteBeforeBus;
        this.routeBeforeBus = ped.routeBeforeBus;

        this.displayRouteAfterBus = ped.displayRouteAfterBus;
        this.stationIndex = ped.stationIndex;
        this.preferredBusLine = ped.preferredBusLine;

        this.stationStart = ped.stationStart;
        this.stationFinish = ped.stationFinish;
    }

    @VisibleForTesting
    Pedestrian(ITimeProvider timeProvider) {
        super(timeProvider, 1, DEFAULT_SPEED, new ArrayList<>());
        preferredBusLine = "";
        displayRouteBeforeBus = new ArrayList<>();
        displayRouteAfterBus = new ArrayList<>();
        routeBeforeBus = new ArrayList<>();
        stationStart = new StationNode(5, 5, 1L, 1);
        stationFinish = new StationNode(5, 10, 2L, 2);
    }

    public StationNode getStartingStation() {
        return (StationNode) uniformRoute.get(stationIndex);
    }

    public StationNode getTargetStation() {
        return (StationNode) uniformRoute.get(stationIndex + 1);
    }

    public String getPreferredBusLine() {
        return preferredBusLine;
    }

    @Override
    public long getAdjacentOsmWayId() {
        return ((LightManagerNode) uniformRoute.get(moveIndex)).getCrossingOsmId1();
        // TODO: remember to consider the crossingosmid2!!!
    }

    @Override
    public String getVehicleType() {
        return VehicleType.PEDESTRIAN.toString();
    }

    public RouteNode findNextStop() {
        for (int i = moveIndex + 1; i < uniformRoute.size(); i++) {
            if (uniformRoute.get(i) instanceof StationNode) {
                return uniformRoute.get(i);
            }
            if (uniformRoute.get(i) instanceof LightManagerNode) {
                return uniformRoute.get(i);
            }
        }
        return null;
    }

    public boolean isAtStation() {
        if (moveIndex == uniformRoute.size()) {
            return false;
        }
        return uniformRoute.get(moveIndex) instanceof StationNode;
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
