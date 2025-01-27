package osmproxy.buses.abstractions;

import osmproxy.buses.BusInfo;
import osmproxy.buses.data.BusInfoData;
import osmproxy.elements.OSMStation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Merge provided data
 */
public interface IDataMerger {
    LinkedHashSet<BusInfo> getBusInfosWithStops(Collection<BusInfoData> busInfoDataSet,
                                                Map<Long, OSMStation> busStops);
}
