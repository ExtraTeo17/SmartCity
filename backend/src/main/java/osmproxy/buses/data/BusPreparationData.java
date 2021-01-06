package osmproxy.buses.data;

import osmproxy.buses.BusInfo;
import osmproxy.elements.OSMStation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("#########################################\n");
        builder.append("All stations:\n[:");
        int i = 0;
        for (OSMStation station : stations.values()) {
            builder.append("stations[").append(i++).append("]=")
                    .append(station.getBusStopId()).append("/")
                    .append(station.getBusStopNr()).append("/")
                    .append(station.getId()).append(":");
        }
        builder.append("]\n");
        builder.append("#########################################");
        return builder.toString();
    }
}
