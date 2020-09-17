package osmproxy.buses;

import mocks.BusApiManagerMock;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import osmproxy.buses.abstractions.IDataMerger;
import osmproxy.buses.data.BusInfoData;
import osmproxy.buses.data.BusPreparationData;
import osmproxy.elements.OSMStation;
import routing.core.IZone;
import routing.core.Zone;
import testutils.XmlParser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static org.mockito.Mockito.when;

class BusDataParserTests {
    private final IZone busZone = Zone.of(52.203342, 20.861213, 300);

    @Test
    public void parseBusData() {
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
        var apiManager = new BusApiManagerMock();
        var parser = new BusDataParser(mockMerger, apiManager, busZone);

        var document = XmlParser.getDocument("DefaultBusZoneData.xml");

        // Act
        parser.parseBusData(document);

        // Assert
        // TODO
    }
}