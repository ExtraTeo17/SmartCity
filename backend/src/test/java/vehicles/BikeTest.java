package vehicles;

import org.junit.jupiter.api.Test;
import smartcity.ITimeProvider;
import vehicles.enums.DrivingState;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BikeTest {
    @Test
    void setState_onInitialState_shouldSetStart() {
        // Arrange
        var time = LocalDateTime.of(2020, 10, 12, 10, 10);
        var timeProvider = mock(ITimeProvider.class);
        when(timeProvider.getCurrentSimulationTime()).thenReturn(time);
        var bike = createBike(timeProvider);

        // Act
        bike.setState(DrivingState.MOVING);

        // Assert
        var start = bike.getStart();
        assertEquals(time, start);
    }

    @Test
    void setState_onFinalState_shouldSetStartAndEnd() {
        // Arrange
        var time = LocalDateTime.of(2020, 10, 12, 10, 10);
        var timeProvider = mock(ITimeProvider.class);
        when(timeProvider.getCurrentSimulationTime()).thenReturn(time);
        var bike = createBike(timeProvider);

        // Act
        bike.setState(DrivingState.AT_DESTINATION);

        // Assert
        var start = bike.getStart();
        assertEquals(time, start);

        var end = bike.getEnd();
        assertEquals(time, end);
    }

    private TestBike createBike(ITimeProvider timeProvider) {
        return new TestBike(timeProvider);
    }
}
