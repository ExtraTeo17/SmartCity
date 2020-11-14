package routing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import routing.core.IZone;
import routing.core.Position;
import routing.core.Zone;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutingHelperTests {
    @ParameterizedTest
    @MethodSource("routing.RoutingData#zones")
    void getRandomPositions_happyPath(IZone zone) {
        // Arrange
        var random = new Random(zone.getRadius());
        var routingHelper = new RoutingHelper(random);

        // Act
        var positions = routingHelper.getRandomPositions(zone);

        // Assert
        assertTrue(zone.contains(positions.first), "Zone should contain start position.\n Pos: " + positions.first.toText());
        assertTrue(zone.contains(positions.second), "Zone should contain end position.\n Pos: " + positions.second.toText());
    }
}