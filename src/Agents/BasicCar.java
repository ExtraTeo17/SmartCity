package Agents;

public class BasicCar extends Vehicle {
    public int Position = 0;
    public int Target = 10;

    @Override
    public String getName() {
        return "Basic Car";
    }

    @Override
    public void Move()
    {
        Position+=1;
        if(Position > Target) Position = 0;
    }
}
