package vehicles.enums;

public enum VehicleType {
    REGULAR_CAR("RegularCar"),
    TEST_CAR("TestCar"),
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
