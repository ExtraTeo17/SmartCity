package osmproxy.buses;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import osmproxy.buses.abstractions.IBusApiManager;
import osmproxy.buses.abstractions.IDataMerger;
import osmproxy.buses.data.BusInfoData;
import osmproxy.buses.data.BusPreparationData;
import osmproxy.elements.OSMStation;
import routing.core.IZone;
import routing.core.Zone;
import testutils.XmlLoader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                .thenReturn(Optional.of(XmlLoader.getDocument("DefaultBusZoneWays.xml")));

        var parser = new BusDataParser(mockMerger, apiManager, defaultBusZone);

        var document = XmlLoader.getDocument("DefaultBusZoneData.xml");

        // Act
        parser.parseBusData(document);

        // Assert
        // TODO - more assertions
        var stationsMap = lambdaContext.stationsMap;
        for (var busData : lambdaContext.busInfoDataSet) {
            var route = busData.busInfo.route;
            for (int i = 0; i < route.size() - 1; ++i) {
                var wayA = route.get(i);
                var wayB = route.get(i + 1);

                var endA = wayA.getWaypoint(0);
                var startB = wayB.getWaypoint(-1);

                assertEquals(endA.getOsmNodeRef(), startB.getOsmNodeRef(), "Bus ways should not be cut in pieces");
            }
        }
    }
}