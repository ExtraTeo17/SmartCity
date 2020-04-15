package Vehicles;

public class BasicCar extends Vehicle {
    int Position = 0;
    int Target = 15;

    int currentTrafficLightID = 8;

    @Override
    public String getVehicleType() {
        return "Basic Car";
    }

    @Override
    public void CalculatePath() {
        Position = 0;
        Target = 15;
        currentTrafficLightID = 8;
    }

    @Override
    public boolean findNextTrafficLight()
    {
        if(Position < currentTrafficLightID)
        return true;
        else if (Position < Target) return false;
        else {
            Position = 0;
            return true;
        }
    }

    @Override
    public String getPositionString() {
        return "Position: " + Position;
    }

    @Override
    public int getCurrentTrafficLightID() {
        return currentTrafficLightID;
    }

    @Override
    public boolean isAtTrafficLights() {
        return Position == currentTrafficLightID;
    }

    @Override
    public boolean isAtDestination() {
        return Position == Target;
    }

    @Override
    public void Move()
    {
        Position+=1;
    }
}
