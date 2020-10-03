package osmproxy.buses;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import osmproxy.buses.abstractions.IApiSerializer;
import osmproxy.buses.abstractions.IBusApiManager;
import osmproxy.buses.serialization.WarszawskieApiSerializer;
import routing.core.IZone;
import routing.core.Zone;
import testutils.FileLoader;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class BusDataParserTests {
    private final IZone defaultBusZone = Zone.of(52.203342, 20.861213, 300);

    @Test
    void parseBusData_onDefaultBusZone() {
        // Arrange
        var apiManager = setupApiManager();
        var apiSerializer = setupApiSerializer();
        var merger = new DataMerger();
        var parser = new BusDataParser(merger, apiSerializer, apiManager, defaultBusZone);
        var document = FileLoader.getDocument("DefaultBusZoneData.xml");

        // Act
        var result = parser.parseBusData(document);

        // Assert
        var allStations = result.stations;
        for (var station : allStations.values()) {
            assertTrue(defaultBusZone.contains(station), "Zone should contain station: " + station);
        }

        for (var busInfo : result.busInfos) {
            var route = busInfo.route;
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

            var stops = busInfo.stops;
            for (var stop : stops) {
                var id = stop.getId();
                assertTrue(allStations.containsKey(id),
                        "Bus stop station: " + id + " should be present in all stations");

                int brigadesCounter = 0;
                int timetablesCounter = 0;
                for (var brigade : busInfo) {
                    ++brigadesCounter;
                    for (var timetable : brigade) {
                        ++timetablesCounter;
                        var time = timetable.getTimeOnStation(id);
                        assertTrue(time.isPresent(), "Bus should have time for each stop.\n" +
                                String.format("Stop_id: %d, brigadeId: %s, boardingTime: %s.", id, brigade.brigadeId,
                                        timetable.getBoardingTime().toString()));
                    }
                }
                assertEquals(5, brigadesCounter);
                assertTrue(timetablesCounter > 0);
            }
        }
    }

    private IBusApiManager setupApiManager() {
        var apiManager = Mockito.mock(IBusApiManager.class);

        var waysDoc = FileLoader.getDocument("DefaultBusZoneWays.xml");
        when(apiManager.getBusWays(ArgumentMatchers.anyList()))
                .thenReturn(Optional.of(waysDoc));

        var brigade1Json = FileLoader.getJsonString("warszawskieApi/line_194_stop_4115_02.json");
        when(apiManager.getBusTimetablesViaWarszawskieAPI("4115", "02", "194"))
                .thenReturn(Optional.of(brigade1Json));

        var brigade2Json = FileLoader.getJsonString("warszawskieApi/line_194_stop_4124_02.json");
        when(apiManager.getBusTimetablesViaWarszawskieAPI("4124", "02", "194"))
                .thenReturn(Optional.of(brigade2Json));

        var brigade3Json = FileLoader.getJsonString("warszawskieApi/line_194_stop_4142_02.json");
        when(apiManager.getBusTimetablesViaWarszawskieAPI("4142", "02", "194"))
                .thenReturn(Optional.of(brigade3Json));

        return apiManager;
    }

    private IApiSerializer setupApiSerializer() {
        var objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new WarszawskieApiSerializer(objectMapper);
    }
}