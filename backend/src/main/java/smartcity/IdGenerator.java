package smartcity;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: Move all static methods to getId/resetId
class IdGenerator {
    public static final int resetValue = 1;
    private final ConcurrentMap<Class<?>, AtomicInteger> idMap;

    private static final AtomicInteger lightManagerId = new AtomicInteger();
    private static final AtomicInteger stationAgentId = new AtomicInteger();
    private static final AtomicInteger pedestrianId = new AtomicInteger();
    private static final AtomicInteger vehicleId = new AtomicInteger();

    IdGenerator() {
        this.idMap = new ConcurrentHashMap<>();
    }

    void register(Class<?> type) {
        idMap.putIfAbsent(type, new AtomicInteger());
    }

    void register(Class<?>... types) {
        for (var type : types) {
            register(type);
        }
    }

    int getId(Class<?> type) {
        return idMap.get(type).getAndIncrement();
    }

    void resetId(Class<?> type) {
        idMap.get(type).set(resetValue);
    }

    @Deprecated
    public static int getLightManagerId() {
        return lightManagerId.getAndIncrement();
    }

    @Deprecated
    public static void resetLightManagerId() {
        lightManagerId.set(resetValue);
    }

    @Deprecated
    public static int getStationAgentId() {
        return stationAgentId.getAndIncrement();
    }

    @Deprecated
    public static void resetStationAgentId() {
        stationAgentId.set(resetValue);
    }

    @Deprecated
    public static int getPedestrianId() {
        return pedestrianId.getAndIncrement();
    }

    @Deprecated
    public static void resetPedestrianId() {
        pedestrianId.set(resetValue);
    }

    @Deprecated
    public static int getVehicleId() {
        return vehicleId.getAndIncrement();
    }

    @Deprecated
    public static void resetVehicleId() {
        vehicleId.set(resetValue);
    }
}
