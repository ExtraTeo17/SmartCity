package vehicles;

import com.google.common.annotations.VisibleForTesting;
import routing.LightManagerNode;
import routing.RouteNode;
import routing.RoutingConstants;
import routing.StationNode;
import routing.core.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("restriction")
// TODO: Create a dedicated super-class for all moving types
public class Pedestrian extends MovingObject {
    private final String preferredBusLine;
    private final List<RouteNode> displayRouteBeforeBus;
    private final List<RouteNode> displayRouteAfterBus;
    private final List<RouteNode> routeBeforeBus;
    private final StationNode stationStart;
    private final StationNode stationFinish;
    private final List<RouteNode> route;

    private transient DrivingState state = DrivingState.STARTING;
    private transient int index = 0;
    private transient int closestLightIndex = 0;
    private transient int stationIndex = 0;

    public Pedestrian(List<RouteNode> routeToStation,
                      List<RouteNode> uniformRouteToStation,
                      List<RouteNode> routeFromStation,
                      List<RouteNode> uniformRouteFromStation,
                      String preferredBusLine,
                      StationNode startStation,
                      StationNode finishStation) {
        super(10);
        this.displayRouteBeforeBus = routeToStation;
        this.routeBeforeBus = uniformRouteToStation;
        this.routeBeforeBus.add(startStation);

        this.displayRouteAfterBus = routeFromStation;

        this.route = new ArrayList<>();
        this.route.addAll(routeBeforeBus);
        this.route.add(finishStation);
        this.route.addAll(uniformRouteFromStation);

        this.stationIndex = routeBeforeBus.size() - 1;
        this.preferredBusLine = preferredBusLine;

        this.stationStart = startStation;
        this.stationFinish = finishStation;
    }

    Pedestrian(Pedestrian ped) {
        super(ped.getSpeed());
        this.displayRouteBeforeBus = ped.displayRouteBeforeBus;
        this.routeBeforeBus = ped.routeBeforeBus;

        this.displayRouteAfterBus = ped.displayRouteAfterBus;
        this.route = ped.route;
        this.stationIndex = ped.stationIndex;
        this.preferredBusLine = ped.preferredBusLine;

        this.stationStart = ped.stationStart;
        this.stationFinish = ped.stationFinish;
    }

    @VisibleForTesting
    Pedestrian() {
        super(10);
        preferredBusLine = "";
        displayRouteBeforeBus = new ArrayList<>();
        displayRouteAfterBus = new ArrayList<>();
        routeBeforeBus = new ArrayList<>();
        route = new ArrayList<>();
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
        return ((LightManagerNode) route.get(index)).getCrossingOsmId1();
    }

    @Override
    public String getVehicleType() {
        return VehicleType.PEDESTRIAN.toString();
    }

    @Override
    public LightManagerNode getNextTrafficLight() {
        for (int i = index + 1; i < route.size(); i++) {
            if (route.get(i) instanceof LightManagerNode) {
                closestLightIndex = i;
                return getCurrentTrafficLightNode();
            }
        }
        closestLightIndex = -1;
        return getCurrentTrafficLightNode();
    }

    public RouteNode findNextStop() {
        for (int i = index + 1; i < route.size(); i++) {
            if (route.get(i) instanceof StationNode) {
                return (StationNode) route.get(i);
            }
            if (route.get(i) instanceof LightManagerNode) {
                return (LightManagerNode) route.get(i);
            }
        }
        return null;
    }

    @Override
    public IGeoPosition getPosition() {
        // TODO: Should not happen - prevent it
        if (index >= route.size()) {
            return route.get(route.size() - 1);
        }

        return route.get(index);
    }

    @Override
    public LightManagerNode getCurrentTrafficLightNode() {
        if (closestLightIndex == -1) {
            return null;
        }
        return (LightManagerNode) (route.get(closestLightIndex));
    }

    @Override
    public boolean isAtTrafficLights() {
        if (index == route.size()) {
            return false;
        }
        return route.get(index) instanceof LightManagerNode;
    }

    public boolean isAtStation() {
        if (index == route.size()) {
            return false;
        }
        return route.get(index) instanceof StationNode;
    }

    @Override
    public boolean isAtDestination() {
        return index == route.size();
    }

    @Override
    public void move() {
        index++;
    }

    @Override
    public List<RouteNode> getDisplayRoute() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMillisecondsToNextLight() {
        return ((closestLightIndex - index) * RoutingConstants.STEP_CONSTANT) / getSpeed();
    }

    @Override
    public DrivingState getState() {
        return state;
    }

    @Override
    public void setState(DrivingState state) {
        this.state = state;
    }

    public List<RouteNode> getDisplayRouteBeforeBus() {
        return displayRouteBeforeBus;
    }

    public List<RouteNode> getDisplayRouteAfterBus() {
        return displayRouteAfterBus;
    }

    public long getMillisecondsToNextStation() {
        return ((routeBeforeBus.size() - 1 - index) * RoutingConstants.STEP_CONSTANT) / getSpeed();

    }

    public StationNode findNextStation() {
        return stationStart;
    }
}
