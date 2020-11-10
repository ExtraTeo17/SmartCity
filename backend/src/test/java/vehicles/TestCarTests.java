package vehicles;

import org.junit.jupiter.api.Test;
import smartcity.ITimeProvider;
import vehicles.enums.DrivingState;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestCarTests {

    @Test
    void setState_onInitialState_shouldSetStart() {
        // Arrange
        var time = LocalDateTime.of(2020, 10, 12, 10, 10);
        var timeProvider = mock(ITimeProvider.class);
        when(timeProvider.getCurrentSimulationTime()).thenReturn(time);
        var car = createCar(timeProvider);

        // Act
        car.setState(DrivingState.MOVING);

        // Assert
        var start = car.getStart();
        assertEquals(time, start);
    }

    @Test
    void setState_onFinalState_shouldSetStartAndEnd() {
        // Arrange
        var time = LocalDateTime.of(2020, 10, 12, 10, 10);
        var timeProvider = mock(ITimeProvider.class);
        when(timeProvider.getCurrentSimulationTime()).thenReturn(time);
        var car = createCar(timeProvider);

        // Act
        car.setState(DrivingState.AT_DESTINATION);

        // Assert
        var start = car.getStart();
        assertEquals(time, start);

        var end = car.getEnd();
        assertEquals(time, end);
    }

    private TestCar createCar() {
        return createCar(mock(ITimeProvider.class));
    }

    private TestCar createCar(ITimeProvider timeProvider) {
        return new TestCar(timeProvider);
    }
}