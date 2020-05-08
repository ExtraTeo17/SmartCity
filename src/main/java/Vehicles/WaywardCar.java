package Vehicles;

import com.graphhopper.util.PointList;
import org.jxmapviewer.viewer.GeoPosition;

public class WaywardCar extends Vehicle {

    PointList route;
    int index = 0;

    boolean passCheck = false;

    public WaywardCar(PointList list) {
        route = list;
    }

    @Override
    public String getVehicleType() {
        return "WaywardCar";
    }

    @Override
    public void CalculateRoute() {

    }

    @Override
    public boolean findNextTrafficLight() {
        return false;
    }

    @Override
    public String getPositionString() {
        return "Lat: " + route.getLat(index) + " Lon: " + route.getLon(index);
    }

    @Override
    public GeoPosition getPosition() {
        return new GeoPosition(route.getLat(index), route.getLon(index));
    }

    @Override
    public int getCurrentTrafficLightID() {
        return 0;
    }

    @Override
    public boolean isAtTrafficLights() {
        return false;
    }

    @Override
    public boolean isAtDestination() {
        return index == route.size() - 1;
    }

    @Override
    public void Move() {
        if(!isAtTrafficLights() || (isAtTrafficLights() && isAllowedToPass()))
        {
            index++;
            setAllowedToPass(false);
        }
    }

    @Override
    public PointList getFullRoute() {
        return route;
    }

    @Override
    public boolean isAllowedToPass() {
        return passCheck;
    }

    @Override
    public void setAllowedToPass(boolean value) {
        passCheck = value;
    }
}
