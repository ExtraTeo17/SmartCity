public class Car {
    VehicleAgent agent = new VehicleAgent(this);
    public int Position = 0;
    public int Target = 10;

    public void Move(){
        Position+=1;
        if(Position > Target) Position = 0;
    }
}
