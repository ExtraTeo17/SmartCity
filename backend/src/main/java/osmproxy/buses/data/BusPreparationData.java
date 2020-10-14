package osmproxy.buses.data;

import osmproxy.buses.BusInfo;
import osmproxy.elements.OSMStation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BusPreparationData implements Serializable {
    public final HashSet<BusInfo> busInfos;
    public final HashMap<Long, OSMStation> stations;

    public BusPreparationData(HashSet<BusInfo> busInfos, HashMap<Long, OSMStation> stations) {
        this.busInfos = busInfos;
        this.stations = stations;
    }

    public BusPreparationData() {
        this.busInfos = new HashSet<>();
        this.stations = new HashMap<>();
    }
}
