package Vehicles;

public abstract class Vehicle {
    public abstract String getVehicleType();
    public abstract void CalculatePath();
    public abstract boolean findNextTrafficLight();
    public abstract String getPositionString();
    public abstract int getCurrentTrafficLightID();
    public abstract boolean isAtTrafficLights();
    public abstract boolean isAtDestination();
    public abstract void Move();

}
