package vehicles;

import org.junit.jupiter.api.Test;
import routing.nodes.RouteNode;

import java.util.ArrayList;
import java.util.List;

class CarTests {

    @Test
    void switchToNextTrafficLight() {
        // Arrange
        var route = new ArrayList<RouteNode>();
        var car = createCar(route);

        // Act

        // Assert
    }

    @Test
    void getCurrentTrafficLightNode() {
        // Arrange

        // Act

        // Assert
    }

    @Test
    void isAtTrafficLights() {
        // Arrange

        // Act

        // Assert
    }

    private static Car createCar(List<RouteNode> route) {
        return new Car(1, new ArrayList<>(), route);
    }
}