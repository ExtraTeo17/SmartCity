package vehicles;

public enum VehicleType {
    REGULAR_CAR("RegularCar"),
    BUS("Bus"),
    PEDESTRIAN("Pedestrian");

    private final String name;

    VehicleType(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
