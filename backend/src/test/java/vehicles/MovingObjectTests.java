package vehicles;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import routing.LightManagerNode;
import routing.RouteNode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;

class MovingObjectTests {

    @ParameterizedTest
    @MethodSource("routesProvider")
    void getNextTrafficLight(String testCaseName, int initialMoveIndex, List<RouteNode> route, int expectedIndex) {
        // Arrange
        var movingObject = new Car(route, route);
        movingObject.moveIndex = initialMoveIndex;

        // Act
        var result = movingObject.getNextTrafficLight();

        // Assert
        assertEquals(expectedIndex, movingObject.closestLightIndex, testCaseName + ": closest light index should match");
        if (expectedIndex < route.size()) {
            assertSame(route.get(expectedIndex), result, testCaseName + ": light manager instance should match");
        }
    }


    static Stream<Arguments> routesProvider() {
        var begRoute = Arrays.asList(createLightManagerNode(), createRouteNode(), createRouteNode());
        var middleRoute = Arrays.asList(createRouteNode(), createLightManagerNode(), createRouteNode());
        var doubledRoute = Arrays.asList(createRouteNode(), createLightManagerNode(), createRouteNode(),
                createLightManagerNode(), createRouteNode(), createRouteNode());
        var endRoute = Arrays.asList(createRouteNode(), createRouteNode(), createLightManagerNode());
        var noneRoute = Arrays.asList(createRouteNode(), createRouteNode(), createRouteNode(), createRouteNode());


        return Stream.of(
                arguments("At beginning - at", 0, begRoute, 0),
                arguments("At beginning - after", 1, begRoute, Integer.MAX_VALUE),
                arguments("In middle - before", 0, middleRoute, 1),
                arguments("In middle - at", 1, middleRoute, 1),
                arguments("In middle - after", 2, middleRoute, Integer.MAX_VALUE),
                arguments("Doubled - before first", 0, doubledRoute, 1),
                arguments("Doubled - at first", 1, doubledRoute, 1),
                arguments("Doubled - after first = before second", 2, doubledRoute, 3),
                arguments("Doubled - at second", 3, doubledRoute, 3),
                arguments("Doubled - after second", 4, doubledRoute, Integer.MAX_VALUE),
                arguments("At end - before", 1, endRoute, 2),
                arguments("At end - at", 2, endRoute, 2),
                arguments("None - start", 0, noneRoute, Integer.MAX_VALUE),
                arguments("None - end", 3, noneRoute, Integer.MAX_VALUE)
        );
    }

    private static LightManagerNode createLightManagerNode() {
        return mock(LightManagerNode.class);
    }

    private static RouteNode createRouteNode() {
        return mock(RouteNode.class);
    }

    @ParameterizedTest
    @MethodSource("routesProvider")
    void isAtTrafficLights(String testCaseName, int initialMoveIndex, List<RouteNode> route, int expectedIndex) {
        // Arrange
        var movingObject = new Car(route, route);
        movingObject.moveIndex = initialMoveIndex;

        // Act
        movingObject.getNextTrafficLight();
        var result = movingObject.isAtTrafficLights();

        // Assert
        assertEquals(initialMoveIndex == expectedIndex, result, testCaseName);
    }
}