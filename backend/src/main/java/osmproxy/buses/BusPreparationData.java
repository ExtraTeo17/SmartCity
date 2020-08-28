package osmproxy.buses;

import osmproxy.elements.OSMStation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BusPreparationData {
    public final Set<BusInfo> busInfos;
    public final HashMap<Long, OSMStation> stations;

    public BusPreparationData(Set<BusInfo> busInfos, HashMap<Long, OSMStation> stations) {
        this.busInfos = busInfos;
        this.stations = stations;
    }

    public BusPreparationData() {
        this.busInfos = new HashSet<>();
        this.stations = new HashMap<>();
    }
}
