package osmproxy.buses;

import osmproxy.elements.OSMStation;

import java.util.Map;
import java.util.Set;

public class BusPreparationData {
    public final Set<BusInfo> busInfos;
    public final Map<Long, OSMStation> stations;

    public BusPreparationData(Set<BusInfo> busInfos, Map<Long, OSMStation> stations) {
        this.busInfos = busInfos;
        this.stations = stations;
    }
}
