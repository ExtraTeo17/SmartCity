package osmproxy.buses;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import osmproxy.buses.abstractions.IApiSerializer;
import osmproxy.buses.abstractions.IBusApiManager;
import osmproxy.buses.abstractions.IDataMerger;
import osmproxy.buses.data.BusInfoData;
import osmproxy.buses.data.BusPreparationData;
import osmproxy.elements.OSMStation;
import routing.core.IZone;
import routing.core.Zone;
import testutils.FileLoader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class BusDataParserTests {
    private final IZone defaultBusZone = Zone.of(52.203342, 20.861213, 300);

    @Test
    void parseBusData() {
        // Arrange
        var mockMerger = Mockito.mock(IDataMerger.class);
        var lambdaContext = new Object() {
            Collection<BusInfoData> busInfoDataSet;
            Map<Long, OSMStation> stationsMap;
        };
        when(mockMerger.getBusInfosWithStops(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .then(ans -> {
                    lambdaContext.busInfoDataSet = ans.getArgument(0);
                    lambdaContext.stationsMap = ans.getArgument(1);
                    return new HashSet<BusPreparationData>();
                });

        var apiManager = Mockito.mock(IBusApiManager.class);
        when(apiManager.getBusWays(ArgumentMatchers.anyList()))
                .thenReturn(Optional.of(FileLoader.getDocument("DefaultBusZoneWays.xml")));
        var apiSerializer = Mockito.mock(IApiSerializer.class);

        var parser = new BusDataParser(mockMerger, apiSerializer, apiManager, defaultBusZone);

        var document = FileLoader.getDocument("DefaultBusZoneData.xml");

        // Act
        parser.parseBusData(document);

        // Assert
        // TODO - more assertions
        for (var busData : lambdaContext.busInfoDataSet) {
            var route = busData.busInfo.route;
            var firstWay = route.get(0);
            assertTrue(firstWay.endsInZone(defaultBusZone), "First way should always end in Zone");
            for (int i = 0; i < route.size() - 1; ++i) {
                var wayA = route.get(i);
                var wayB = route.get(i + 1);

                var endA = wayA.getWaypoint(-1);
                var startB = wayB.getWaypoint(0);

                assertEquals(endA.getOsmNodeRef(), startB.getOsmNodeRef(),
                        "Bus ways should not be reversed or cut in pieces");
            }
        }
    }
}