package osmproxy.buses;

import osmproxy.buses.abstractions.IDataMerger;
import osmproxy.buses.data.BusInfoData;
import osmproxy.elements.OSMStation;

import java.util.*;

public class DataMerger implements IDataMerger {
    @Override
    public LinkedHashSet<BusInfo> getBusInfosWithStops(Collection<BusInfoData> busInfoDataSet,
                                                       Map<Long, OSMStation> busStopMap) {
        var busInfos = new LinkedHashSet<BusInfo>();
        for (var busInfoData : busInfoDataSet) {
            List<OSMStation> validBusStops = new ArrayList<>(busInfoData.busStopIds.size());
            for (var id : busInfoData.busStopIds) {
                var station = busStopMap.get(id);
                if (station != null) {
                    // WARN: Station is not copied here - should not be modified in any way
                    validBusStops.add(station);
                }
            }
            var info = busInfoData.busInfo;
            info.addStops(validBusStops);
            busInfos.add(info);
        }

        return busInfos;
    }
}
