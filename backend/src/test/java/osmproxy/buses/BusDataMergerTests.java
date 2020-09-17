package osmproxy.buses;

import org.junit.Assert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import osmproxy.buses.data.BusInfoData;
import osmproxy.elements.OSMElement;
import osmproxy.elements.OSMStation;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;


class BusDataMergerTests {
    private final Random random = new Random();

    @ParameterizedTest
    @MethodSource("stopIdsProvider")
    void getBusInfosWithStops_manySets_correctResult(long[][] stopsSets, String testCaseName) {
        // Arrange
        random.setSeed(30);
        var merger = new DataMerger();
        String lineName = "test";
        var busInfoDataSet = generateBusDataSet(lineName, stopsSets);
        var busStopsSet = generateStations(stopsSets);

        // Act
        var result = merger.getBusInfosWithStops(busInfoDataSet, busStopsSet);

        // Assert
        for (var info : result) {
            var stopIds = info.stops.stream()
                    .map(OSMElement::getId).mapToLong(l -> l).toArray();
            var line = info.busLine;
            var lineIndex = Integer.parseInt(line.substring(lineName.length()));
            Assert.assertArrayEquals(testCaseName + ": Invalid set\n", stopIds, stopsSets[lineIndex]);
        }
    }

    static Stream<Arguments> stopIdsProvider() {
        return Stream.of(arguments(
                new long[][]{
                        {1, 2, 3, 4},
                        {5, 6, 7, 8},
                        {1, 2, 3, 4, 5, 6, 7, 8}
                }, "OneUnionSet"),
                arguments(new long[][]{
                        {1, 2, 3},
                        {5, 6, 7},
                        {8, 9, 10}
                }, "DistinctSets"),
                arguments(new long[][]{
                        {1, 2, 3, 4},
                        {2, 3, 1, 4},
                        {8, 5, 6, 7},
                        {5, 6, 7, 8}
                }, "TwoIdentical"),
                arguments(new long[][]{
                        {1, 2, 3, 4},
                        {4, 5, 6, 7},
                        {1, 4, 9, 10}
                }, "OneFromBoth"),
                arguments(new long[][]{
                        {},
                        {},
                        {11, 4, 9, 10}
                }, "TwoEmptySets"),
                arguments(new long[][]{
                        {1, 2, 3, 4},
                        {1, 2, 3}, {1, 2, 4}, {1, 3, 4}, {2, 3, 4},
                        {1, 2}, {1, 3}, {1, 4},
                        {2, 3}, {2, 4},
                        {3, 4},
                        {1}, {2}, {3}, {4},
                        {},
                }, "AllSubsets"),
                arguments(new long[][]{
                        {1, 2, 3}, {1, 3, 2}, {2, 1, 3}, {2, 3, 1}, {3, 1, 2}, {3, 2, 1},
                        {1, 2}, {1, 3},
                        {2, 1}, {2, 3},
                        {3, 1}, {3, 2},
                        {1}, {2}, {3}, {4},
                        {},
                }, "AllSubsetsOrdered")
        );
    }

    private Set<BusInfoData> generateBusDataSet(String line, long[][] stopIdsPerInfo) {
        var result = new HashSet<BusInfoData>();
        for (int infoIt = 0; infoIt < stopIdsPerInfo.length; ++infoIt) {
            var info = new BusInfo(line + infoIt, new ArrayList<>());
            var idsList = Arrays.stream(stopIdsPerInfo[infoIt]).boxed().collect(Collectors.toList());
            result.add(new BusInfoData(info, idsList));
        }

        return result;
    }

    private Map<Long, OSMStation> generateStations(long[][] idSubSets) {
        var result = new HashMap<Long, OSMStation>();
        var idsSet = new HashSet<Long>();
        for (var subset : idSubSets) {
            idsSet.addAll(Arrays.stream(subset).boxed().collect(Collectors.toList()));
        }

        for (var id : idsSet) {
            var lat = random.nextDouble();
            var lng = random.nextDouble();
            result.put(id, new OSMStation(id, lat, lng, "ref" + id));
        }

        return result;
    }
}