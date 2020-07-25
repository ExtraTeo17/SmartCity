package vehicles;

public enum VehicleType {
    REGULAR_CAR("RegularCar"),
    BUS("Bus"),
    PEDESTRIAN("Pedestrian");

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
