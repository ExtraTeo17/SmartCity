package vehicles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import osmproxy.buses.Timetable;
import smartcity.ITimeProvider;
import testutils.ReflectionHelper;
import vehicles.enums.BusFillState;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;

class BusTests {

    @Test
    void getFillState_onLowCapacity_shouldReturnCorrectResult() {
        // Arrange
        var bus = createBus();

        // Act
        var state = bus.getFillState();
        var passengerCount = bus.getPassengersCount();

        // Assert
        assertEquals(0, passengerCount);
        assertEquals(BusFillState.LOW, state);
    }

    @Test
    void getFillState_onMidCapacity_shouldReturnCorrectResult() {
        // Arrange
        var bus = createBus();
        for (int i = 0; i <= Bus.CAPACITY_MID; ++i) {
            bus.increasePassengersCount();
        }

        // Act
        var state = bus.getFillState();
        var passengerCount = bus.getPassengersCount();

        // Assert
        assertTrue(passengerCount > Bus.CAPACITY_MID);
        assertEquals(BusFillState.MID, state);
    }

    @Test
    void getFillState_onHighCapacity_shouldReturnCorrectResult() {
        // Arrange
        var bus = createBus();
        for (int i = 0; i <= Bus.CAPACITY_HIGH; ++i) {
            bus.increasePassengersCount();
        }

        // Act
        var state = bus.getFillState();
        var passengerCount = bus.getPassengersCount();

        // Assert
        assertTrue(passengerCount > Bus.CAPACITY_HIGH);
        assertEquals(BusFillState.HIGH, state);
    }

    @ParameterizedTest
    @MethodSource("increasingStatesProvider")
    void increasePassengersCount_shouldChangePassengersCountAndFillState(String testCaseName,
                                                                         int initialCount, BusFillState newState) {
        // Arrange
        var bus = createBus();
        setupPassengerCount(bus, initialCount);

        // Act
        bus.increasePassengersCount();

        // Assert
        var state = bus.getFillState();
        var passengerCount = bus.getPassengersCount();
        assertTrue(passengerCount > initialCount, testCaseName);
        assertEquals(newState, state, testCaseName);
    }

    static Stream<Arguments> increasingStatesProvider() {
        return Stream.of(arguments("low_to_mid", Bus.CAPACITY_MID, BusFillState.MID),
                arguments("mid_to_high", Bus.CAPACITY_HIGH, BusFillState.HIGH),
                arguments("low_to_low", 0, BusFillState.LOW),
                arguments("mid_to_mid", Bus.CAPACITY_MID + 1, BusFillState.MID),
                arguments("high_to_high", Bus.CAPACITY_HIGH + 1, BusFillState.HIGH)
        );
    }

    @ParameterizedTest
    @MethodSource("decreasingStatesProvider")
    void decreasePassengersCount_shouldChangePassengersCountAndFillState(String testCaseName,
                                                                         int initialCount, BusFillState newState) {
        // Arrange
        var bus = createBus();
        setupPassengerCount(bus, initialCount);

        // Act
        bus.decreasePassengersCount();

        // Assert
        var state = bus.getFillState();
        var passengerCount = bus.getPassengersCount();
        assertTrue(passengerCount < initialCount, testCaseName);
        assertEquals(newState, state, testCaseName);
    }

    static Stream<Arguments> decreasingStatesProvider() {
        return Stream.of(arguments("mid_to_low", Bus.CAPACITY_MID + 1, BusFillState.LOW),
                arguments("high_to_mid", Bus.CAPACITY_HIGH + 1, BusFillState.MID),
                arguments("low_to_low", Bus.CAPACITY_MID, BusFillState.LOW),
                arguments("mid_to_mid", Bus.CAPACITY_MID + 2, BusFillState.MID),
                arguments("high_to_high", Bus.CAPACITY_HIGH + 2, BusFillState.HIGH)
        );
    }


    private static Bus createBus() {
        return new Bus(mock(ITimeProvider.class), 1, new ArrayList<>(), new ArrayList<>(),
                mock(Timetable.class), "523", "01");
    }

    private static void setupPassengerCount(Bus bus, int count){
        ReflectionHelper.setField("passengersCount", bus, count);
        if(count > Bus.CAPACITY_HIGH){
            ReflectionHelper.setField("fillState", bus, BusFillState.HIGH);
        }
        else if(count > Bus.CAPACITY_MID){
            ReflectionHelper.setField("fillState", bus, BusFillState.MID);
        }
    }
}