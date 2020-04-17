package Vehicles;

import com.graphhopper.util.PointList;
import org.jxmapviewer.viewer.GeoPosition;

public class DummyCar extends Vehicle {

    @Override
    public String getVehicleType() {
        return null;
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
        return null;
    }

    @Override
    public GeoPosition getPosition() {
        return null;
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
        return false;
    }

    @Override
    public void Move() {

    }

    @Override
    public PointList getFullRoute() {
        return null;
    }
}
