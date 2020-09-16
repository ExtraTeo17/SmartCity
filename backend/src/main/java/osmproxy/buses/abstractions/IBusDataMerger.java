package osmproxy.buses.abstractions;

import osmproxy.buses.BusInfo;
import osmproxy.buses.data.BusInfoData;
import osmproxy.elements.OSMStation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface IBusDataMerger {
    Set<BusInfo> getBusInfosWithStops(Collection<BusInfoData> busInfoDataSet,
                                      Map<Long, OSMStation> busStops);
}
