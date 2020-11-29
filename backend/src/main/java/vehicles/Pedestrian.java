package vehicles;

import com.google.common.annotations.VisibleForTesting;
import routing.RoutingConstants;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;
import smartcity.ITimeProvider;
import smartcity.task.abstractions.ITaskProvider;
import vehicles.enums.VehicleType;

import java.util.ArrayList;
import java.util.List;

public class Pedestrian extends MovingObject {
    private  ITaskProvider taskProvider = null;
    private  List<RouteNode> displayRouteBeforeBus;
    private  List<RouteNode> displayRouteAfterBus;
    private  List<RouteNode> routeBeforeBus;
    private  StationNode stationStart;
    private final StationNode stationFinish;
    private boolean troubled = false;

    private transient int stationIndex = 0;

    public Pedestrian(int agentId,
                      List<RouteNode> routeToStation,
                      List<RouteNode> uniformRouteToStation,
                      List<RouteNode> routeFromStation,
                      List<RouteNode> uniformRouteFromStation,
                      StationNode startStation,
                      StationNode finishStation,
                      ITimeProvider timeProvider,
                      ITaskProvider taskProvider) {
        super(timeProvider, agentId, 10, createRoute(startStation, uniformRouteToStation,
                finishStation, uniformRouteFromStation));
        this.displayRouteBeforeBus = routeToStation;
        this.routeBeforeBus = uniformRouteToStation;
        this.routeBeforeBus.add(startStation);

        this.displayRouteAfterBus = routeFromStation;
        this.stationIndex = routeBeforeBus.size() - 1;
        this.stationStart = startStation;
        this.stationFinish = finishStation;
        this.taskProvider = taskProvider;
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

        this.stationStart = ped.stationStart;
        this.stationFinish = ped.stationFinish;
        this.taskProvider = ped.taskProvider;
    }

    @VisibleForTesting
    Pedestrian(ITimeProvider timeProvider) {
        super(timeProvider, 1, 10, new ArrayList<>());
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
    public StationNode getStationFinish(){return stationFinish;}

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
        return getMillisecondsOnRoute(routeBeforeBus, moveIndex);
    }

    public StationNode findNextStation() {
        return stationStart;
    }
    
    public void setTroubled(boolean troubled) {
    	this.troubled = troubled;
    }

	public boolean isTroubled() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setDisplayRouteBeforeBus(List<RouteNode> displayRouteToStation){
        this.displayRouteBeforeBus = displayRouteToStation;
    }
    public void setRouteBeforeBus(List<RouteNode> routeBeforeBus)
    {
        this.routeBeforeBus = routeBeforeBus;
    }
    public void setStationStart(StationNode newStationStart)
    {
        this.stationStart = newStationStart ;
    }
    public void setStationIndex(int index)
    {
        this.stationIndex = index ;
    }

}
