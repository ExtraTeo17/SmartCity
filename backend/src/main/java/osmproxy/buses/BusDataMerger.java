package osmproxy.buses;

import osmproxy.buses.abstractions.IBusDataMerger;
import osmproxy.buses.data.BusInfoData;
import osmproxy.elements.OSMStation;

import java.util.*;

public class BusDataMerger implements IBusDataMerger {
    @Override
    public Set<BusInfo> getBusInfosWithStops(Collection<BusInfoData> busInfoDataSet,
                                             Map<Long, OSMStation> busStops) {
        var busInfos = new LinkedHashSet<BusInfo>();
        for (var busInfoData : busInfoDataSet) {
            List<OSMStation> validBusStops = new ArrayList<>(busInfoData.busStopIds.size());
            for (var id : busInfoData.busStopIds) {
                var station = busStops.get(id);
                if (station != null) {
                    // WARN: Station is not copied here - should not be modified in any way
                    validBusStops.add(station);
                }
            }
            var info = busInfoData.busInfo;
            info.setStops(validBusStops);
            busInfos.add(info);
        }

        return busInfos;
    }
}
