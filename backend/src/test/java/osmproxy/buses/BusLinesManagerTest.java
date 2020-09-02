package osmproxy.buses;

import com.google.common.primitives.Longs;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import osmproxy.elements.OSMElement;
import osmproxy.elements.OSMStation;
import routing.core.IZone;
import routing.core.Zone;

import java.util.*;
import java.util.stream.Collectors;


class BusLinesManagerTest {
    private final IZone busZone = Zone.of(52.203342, 20.861213, 300);
    private final Random random = new Random(38);

    @BeforeEach
    void setUp() {
    }

    @Test
    void getBusInfosWithStops_happyPath() {
        // Arrange
        BusLinesManager manager = new BusLinesManager(busZone);
        long[] stopsA = {1, 2, 3, 4};
        long[] stopsB = {5, 6, 7, 8};
        long[] stopsAB = Longs.concat(stopsA, stopsB);
        long[][] stopsSets = {stopsA, stopsB, stopsAB};

        String lineName = "test";
        var busInfoDataSet = generateBusDataSet(lineName, stopsSets);
        var busStopsSet = generateStations(stopsAB);

        // Act
        var result = manager.getBusInfosWithStops(busInfoDataSet, busStopsSet);

        // Assert
        for (var info : result) {
            var stopIds = info.getStops().stream()
                    .map(OSMElement::getId).mapToLong(l -> l).toArray();
            var line = info.getBusLine();
            var lineIndex = Integer.parseInt(line.substring(lineName.length()));
            Assert.assertArrayEquals(stopIds, stopsSets[lineIndex]);
        }
    }

    private Set<BusLinesManager.BusInfoData> generateBusDataSet(String line, long[][] stopIdsPerInfo) {
        var result = new HashSet<BusLinesManager.BusInfoData>();
        for (int infoIt = 0; infoIt < stopIdsPerInfo.length; ++infoIt) {
            var info = new BusInfo(line + infoIt, new ArrayList<>());
            var idsList = Arrays.stream(stopIdsPerInfo[infoIt]).boxed().collect(Collectors.toList());
            result.add(new BusLinesManager.BusInfoData(info, idsList));
        }

        return result;
    }

    private Set<OSMStation> generateStations(long[]... idSubSets) {
        var result = new HashSet<OSMStation>();
        var idsSet = new HashSet<Long>();
        for (var subset : idSubSets) {
            idsSet.addAll(Arrays.stream(subset).boxed().collect(Collectors.toList()));
        }

        for (var id : idsSet) {
            var lat = random.nextDouble();
            var lng = random.nextDouble();
            result.add(new OSMStation(id, lat, lng, "ref" + id));
        }

        return result;
    }
}