package agents;

import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import routing.nodes.StationNode;
import smartcity.ITimeProvider;
import smartcity.config.abstractions.IChangeTransportConfigContainer;
import utilities.Siblings;
import vehicles.Bus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BusAgentTests {

    @Test
    void shouldReturnTwoSubsequentStations() {
        // Arrange
        Bus bus = mock(Bus.class);
        BusAgent busAgent = new BusAgent(12345, bus, mock(ITimeProvider.class),
                mock(EventBus.class), mock(IChangeTransportConfigContainer.class));
        Random random = new Random(54321);
        List<StationNode> stationsOnBusRoute = Arrays.asList(
                new StationNode(52.2033840, 20.8649501, 2508330388L, 1), // Przemyslawa II 02
                new StationNode(52.2031155, 20.8627465, 2508330382L, 2), // Boleslawa Smialego 02
                new StationNode(52.2036584, 20.8596403, 2508330380L, 3), // Boleslawa Krzywoustego 02
                new StationNode(52.2073158, 20.8595175, 4016884902L, 4)); // Zielonej Gesi 01
        when(bus.getStationNodesOnRoute()).thenReturn(stationsOnBusRoute);

        // Act
        Optional<Siblings<StationNode>> stationsOpt = busAgent.getTwoSubsequentStations(random);

        // Assert
        assertTrue(stationsOpt.isPresent());
        int firstStationIndex = stationsOnBusRoute.indexOf(stationsOpt.get().first);
        int secondStationIndex = stationsOnBusRoute.indexOf(stationsOpt.get().second);
        assertTrue(secondStationIndex > firstStationIndex);
    }
}
