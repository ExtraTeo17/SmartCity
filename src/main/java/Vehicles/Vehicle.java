package Vehicles;

import com.graphhopper.util.PointList;
import org.jxmapviewer.viewer.GeoPosition;

public abstract class Vehicle {
    public abstract String getVehicleType();
    public abstract void CalculateRoute();
    public abstract boolean findNextTrafficLight();
    public abstract String getPositionString();
    public abstract GeoPosition getPosition();
    public abstract int getCurrentTrafficLightID();
    public abstract boolean isAtTrafficLights();
    public abstract boolean isAtDestination();
    public abstract void Move();
    public abstract PointList getFullRoute();
}
