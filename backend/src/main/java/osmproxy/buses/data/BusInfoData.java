package osmproxy.buses.data;

import osmproxy.buses.BusInfo;

import java.util.List;

public class BusInfoData {
    public final BusInfo busInfo;
    public final List<Long> busStopIds;

    public BusInfoData(BusInfo busInfo, List<Long> busStopIds) {
        this.busInfo = busInfo;
        this.busStopIds = busStopIds;
    }
}
