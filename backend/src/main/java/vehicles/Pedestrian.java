package vehicles;

import routing.LightManagerNode;
import routing.RouteNode;
import routing.Router;
import routing.StationNode;
import routing.core.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("restriction")
// TODO: Create a dedicated super-class for all moving types
public class Pedestrian extends MovingObject {

    private final String preferredBusLine;
    public DrivingState state = DrivingState.STARTING;
    private final List<RouteNode> displayRouteBeforeBus;
    private final List<RouteNode> displayRouteAfterBus;
    private final List<RouteNode> routeBeforeBus;
    private final StationNode stationStart;
    private final StationNode stationFinish;
    private final List<RouteNode> route = new ArrayList<>();
    private int index = 0;
    private int closestLightIndex = 0;
    private int stationIndex = 0;

    public Pedestrian(List<RouteNode> routeToStation, List<RouteNode> routeFromStation,
                      String preferredBusLine, StationNode startStation, StationNode finishStation) {
        super(10);
        displayRouteBeforeBus = routeToStation;
        displayRouteAfterBus = routeFromStation;
        routeBeforeBus = Router.uniformRoute(displayRouteBeforeBus);
        routeBeforeBus.add(startStation);
        List<RouteNode> routeAfterBus = Router.uniformRoute(displayRouteAfterBus);
        routeAfterBus.add(0, finishStation);
        route.addAll(routeBeforeBus);
        route.addAll(routeAfterBus);
        stationIndex = routeBeforeBus.size() - 1;
        this.preferredBusLine = preferredBusLine;
        stationStart = startStation;
        stationFinish = finishStation;
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
        return ((closestLightIndex - index) * Router.STEP_CONSTANT) / getSpeed();
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
        return ((routeBeforeBus.size() - 1 - index) * Router.STEP_CONSTANT) / getSpeed();

    }

    public StationNode findNextStation() {
        return stationStart;
    }
}
