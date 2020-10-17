package vehicles;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;

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
    void switchToNextTrafficLight(String testCaseName, int initialMoveIndex, List<RouteNode> route, int expectedLightIndex) {
        // Arrange
        var movingObject = new Car(1, route, route);
        movingObject.moveIndex = initialMoveIndex;

        // Act
        var result = movingObject.switchToNextTrafficLight();

        // Assert
        assertEquals(expectedLightIndex, movingObject.closestLightIndex, testCaseName + ": closest light index should match");
        if (expectedLightIndex < route.size()) {
            assertSame(route.get(expectedLightIndex), result, testCaseName + ": light manager instance should match");
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
                arguments("At beginning - at", 0, begRoute, Integer.MAX_VALUE),
                arguments("At beginning - after", 1, begRoute, Integer.MAX_VALUE),
                arguments("In middle - before", 0, middleRoute, 1),
                arguments("In middle - at", 1, middleRoute, Integer.MAX_VALUE),
                arguments("In middle - after", 2, middleRoute, Integer.MAX_VALUE),
                arguments("Doubled - before first", 0, doubledRoute, 1),
                arguments("Doubled - at first", 1, doubledRoute, 3),
                arguments("Doubled - after first = before second", 2, doubledRoute, 3),
                arguments("Doubled - at second", 3, doubledRoute, Integer.MAX_VALUE),
                arguments("Doubled - after second", 4, doubledRoute, Integer.MAX_VALUE),
                arguments("At end - before", 1, endRoute, 2),
                arguments("At end - at", 2, endRoute, Integer.MAX_VALUE),
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
    void isAtTrafficLights(String testCaseName, int initialMoveIndex, List<RouteNode> route) {
        // Arrange
        var movingObject = new Car(1, route, route);
        movingObject.moveIndex = initialMoveIndex;

        // Act
        var result = movingObject.isAtTrafficLights();

        // Assert
        assertEquals(movingObject.uniformRoute.get(initialMoveIndex) instanceof LightManagerNode, result, testCaseName);
    }

    @ParameterizedTest
    @MethodSource("routesProvider")
    void getCurrentTrafficLightNode(String testCaseName,
                                    @SuppressWarnings("unused") int initialMoveIndex,
                                    List<RouteNode> route) {
        // Arrange
        var movingObject = new Car(1, route, route);

        // Act
        var result = movingObject.switchToNextTrafficLight();
        var lightNode = movingObject.getCurrentTrafficLightNode();

        // Assert
        assertSame(result, lightNode, testCaseName + ": closest light index should match");
    }
}