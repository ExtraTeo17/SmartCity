package vehicles;

import org.junit.jupiter.api.Test;
import smartcity.ITimeProvider;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestPedestrianTests {

    @Test
    void setState_onInitialState_shouldSetStart() {
        // Arrange
        var time = LocalDateTime.of(2020, 10, 12, 10, 10);
        var timeProvider = mock(ITimeProvider.class);
        when(timeProvider.getCurrentSimulationTime()).thenReturn(time);
        var pedestrian = createPedestrian(timeProvider);

        // Act
        pedestrian.setState(DrivingState.MOVING);

        // Assert
        var start = pedestrian.getStart();
        assertEquals(time, start);
    }

    @Test
    void setState_onFinalState_shouldSetStartAndEnd() {
        // Arrange
        var time = LocalDateTime.of(2020, 10, 12, 10, 10);
        var timeProvider = mock(ITimeProvider.class);
        when(timeProvider.getCurrentSimulationTime()).thenReturn(time);
        var pedestrian = createPedestrian(timeProvider);

        // Act
        pedestrian.setState(DrivingState.AT_DESTINATION);

        // Assert
        var start = pedestrian.getStart();
        assertEquals(time, start);

        var end = pedestrian.getEnd();
        assertEquals(time, end);
    }


    private TestPedestrian createPedestrian() {
        return createPedestrian(mock(ITimeProvider.class));
    }

    private TestPedestrian createPedestrian(ITimeProvider timeProvider) {
        return new TestPedestrian(timeProvider);
    }
}