package vehicles;

import org.junit.jupiter.api.Test;
import smartcity.ITimeProvider;
import smartcity.task.abstractions.ITaskProvider;
import vehicles.enums.DrivingState;

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
        var taskProvider = mock(ITaskProvider.class);
        when(timeProvider.getCurrentSimulationTime()).thenReturn(time);
        var pedestrian = createPedestrian(timeProvider, taskProvider);

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
        var taskProvider = mock(ITaskProvider.class);
        when(timeProvider.getCurrentSimulationTime()).thenReturn(time);
        var pedestrian = createPedestrian(timeProvider, taskProvider);

        // Act
        pedestrian.setState(DrivingState.AT_DESTINATION);

        // Assert
        var start = pedestrian.getStart();
        assertEquals(time, start);

        var end = pedestrian.getEnd();
        assertEquals(time, end);
    }


    private TestPedestrian createPedestrian() {
        return createPedestrian(mock(ITimeProvider.class), mock(ITaskProvider.class));
    }

    private TestPedestrian createPedestrian(ITimeProvider timeProvider, ITaskProvider taskProvider) {
        return new TestPedestrian(timeProvider);
    }
}