package smartcity;

import java.util.concurrent.atomic.AtomicInteger;

// TODO: Instance class via DI + Id based on string/enum/class in dictionary
class IdGenerator {
    private static final int resetValue = 1;
    private static final AtomicInteger lightManagerId = new AtomicInteger();
    private static final AtomicInteger stationAgentId = new AtomicInteger();
    private static final AtomicInteger busId = new AtomicInteger();
    private static final AtomicInteger pedestrianId = new AtomicInteger();
    private static final AtomicInteger vehicleId = new AtomicInteger();

    public static int getLightManagerId() {
        return lightManagerId.getAndIncrement();
    }

    public static void resetLightManagerId(){
        lightManagerId.set(resetValue);
    }

    public static int getStationAgentId() {
        return stationAgentId.getAndIncrement();
    }

    public static void resetStationAgentId(){
        stationAgentId.set(resetValue);
    }

    public static int getBusId() {
        return busId.getAndIncrement();
    }

    public static void resetBusId(){
        busId.set(resetValue);
    }

    public static int getPedestrianId() {
        return pedestrianId.getAndIncrement();
    }

    public static void resetPedestrianId(){
        pedestrianId.set(resetValue);
    }

    public static int getVehicleId() {
        return vehicleId.getAndIncrement();
    }

    public static void resetVehicleId(){
        vehicleId.set(resetValue);
    }
}
