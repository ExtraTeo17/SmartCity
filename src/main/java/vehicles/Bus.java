package vehicles;

import gui.MapWindow;
import org.jxmapviewer.viewer.GeoPosition;
import routing.LightManagerNode;
import routing.RouteNode;
import routing.Router;
import routing.StationNode;
import smartcity.buses.Timetable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Bus extends MovingObject {

    public static int CAPACITY_MID = 10;
    public static int CAPACITY_HIGH = 25;

    private final Timetable timetable;
    private final HashMap<Integer, List<String>> stationsForPassengers = new HashMap<>();
    private final List<StationNode> stationNodesOnRoute;
    private final String busLine;
    private final String brigadeNr;
    public DrivingState State = DrivingState.STARTING;
    private List<RouteNode> displayRoute;
    private List<RouteNode> route;
    private int index = 0;
    private int speed = 40;
    private int closestLightIndex = -1;
    private int closestStationIndex = -1;
    private int passengersCount = 0;

    public Bus(final List<RouteNode> route, final Timetable timetable, final String busLine,
               final String brigadeNr) {
        displayRoute = route;

        for (RouteNode node : route) {
            if (node instanceof StationNode) {
                StationNode station = (StationNode) node;
                stationsForPassengers.put(station.getStationId(), new ArrayList<>());
            }
        }

        this.route = Router.uniformRoute(displayRoute);
        this.timetable = timetable;
        stationNodesOnRoute = extractStationsFromRoute();
        this.busLine = busLine;
        this.brigadeNr = brigadeNr;
    }

    public int getPassengersCount() {
        return passengersCount;
    }

    public void addPassengerToStation(int id, String passenger) {
        stationsForPassengers.get(id).add(passenger);
        passengersCount++;
    }

    public boolean removePassengerFromStation(int id, String passenger) {
        if (stationsForPassengers.get(id).remove(passenger)) {
            passengersCount--;
            return true;
        }
        return false;
    }

    public List<String> getPassengersToLeave(int id) {
        return stationsForPassengers.get(id);
    }

    private List<StationNode> extractStationsFromRoute() {
        List<StationNode> stationsOnRoute = new ArrayList<>();
        for (final RouteNode node : route) {
            if (node instanceof StationNode) {
                stationsOnRoute.add((StationNode) node);
            }
        }
        return stationsOnRoute;
    }

    public final String getLine() {
        return busLine;
    }

    public final List<StationNode> getStationNodesOnRoute() {
        return stationNodesOnRoute;

    }

    @Override
    public long getAdjacentOsmWayId() {
        return ((LightManagerNode) route.get(index)).getOsmWayId();
    }

    @Override
    public String getVehicleType() {
        return VehicleType.BUS.toString();
    }

    @Override
    public LightManagerNode findNextTrafficLight() {
        for (int i = index + 1; i < route.size(); i++) {
            if (route.get(i) instanceof LightManagerNode) {
                closestLightIndex = i;
                return getCurrentTrafficLightNode();
            }
        }
        closestLightIndex = -1;
        return getCurrentTrafficLightNode();
    }

    public StationNode findNextStation() {
        for (int i = index + 1; i < route.size(); i++) {
            if (route.get(i) instanceof StationNode) {
                closestStationIndex = i;
                return (StationNode) route.get(i);
            }
        }
        closestStationIndex = -1;
        return null;
    }

    public Date getTimeOnStation(String osmStationId) {
        return timetable.getTimeOnStation(Long.parseLong(osmStationId));

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
    public String getPositionString() {
        return "Lat: " + route.get(index).getLatitude() + " Lon: " + route.get(index).getLongitude();
    }

    @Override
    public GeoPosition getPosition() {
        return new GeoPosition(route.get(index).getLatitude(), route.get(index).getLongitude());
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
            return true;
        }
        return route.get(index) instanceof StationNode;
    }

    public StationNode getCurrentStationNode() {
        if (closestStationIndex == -1) {
            return null;
        }
        return (StationNode) (route.get(closestStationIndex));
    }

    @Override
    public boolean isAtDestination() {
        return index == route.size();
    }

    @Override
    public void Move() {
        if (isAtDestination()) {
            index = 0;
        }
        else {
            index++;
        }
    }

    @Override
    public List<RouteNode> getDisplayRoute() {
        return displayRoute;
    }

    @Override
    public int getMillisecondsToNextLight() {
        return ((closestLightIndex - index) * 3600) / getSpeed();
    }

    public int getMillisecondsToNextStation() {
        return ((closestStationIndex - index) * 3600) / getSpeed();
    }

    @Override
    public int getSpeed() {
        return speed * MapWindow.getTimeScale();
    }

    @Override
    public DrivingState getState() {
        return State;
    }

    @Override
    public void setState(DrivingState state) {
        State = state;
    }

    public Date getBoardingTime() {
        return timetable.getBoardingTime();
    }


}
