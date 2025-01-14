package vehicles.enums;

public enum VehicleType {
    REGULAR_CAR("RegularCar"),
    BIKE("Bike"),
    TEST_CAR("TestCar"),
    TEST_BIKE("TestBike"),
    BUS("Bus"),
    PEDESTRIAN("Pedestrian"),
    TEST_PEDESTRIAN("TestPedestrian");

    private final String name;

    VehicleType(String name) {
        this.name = name;
    }

    public static VehicleType getValue(String name) {
        for (var vehicleType : VehicleType.values()) {
            if (vehicleType.name.equals(name)) {
                return vehicleType;
            }
        }

        throw new IllegalArgumentException("No such value exists: " + name);
    }

    @Override
    public String toString() {
        return name;
    }
}
