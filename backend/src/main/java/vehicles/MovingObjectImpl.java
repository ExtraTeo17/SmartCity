package vehicles;

import gui.MapWindow;
import org.jxmapviewer.viewer.GeoPosition;
import routing.LightManagerNode;
import routing.RouteNode;
import routing.Router;

import java.util.List;

public class MovingObjectImpl extends MovingObject {
    public DrivingState State = DrivingState.STARTING;
    private List<RouteNode> displayRoute;
    private List<RouteNode> route;
    private int index = 0;
    private int speed = 50;
    private int closestLightIndex = 0;

    public MovingObjectImpl(List<RouteNode> info) {
        displayRoute = info;
        route = Router.uniformRoute(displayRoute);
    }

    @Override
    public long getAdjacentOsmWayId() {
        while (!(route.get(index) instanceof LightManagerNode)) {
            index--;
        }
        return ((LightManagerNode) route.get(index)).getOsmWayId();
    }

    @Override
    public String getVehicleType() {
        return VehicleType.REGULAR_CAR.toString();
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

    @Override
    public boolean isAtDestination() {
        return index == route.size();
    }

    @Override
    public void Move() {
        index++;
    }

    @Override
    public List<RouteNode> getDisplayRoute() {
        return displayRoute;
    }

    @Override
    public int getMillisecondsToNextLight() {
        return ((closestLightIndex - index) * 3600) / getSpeed();
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
}
