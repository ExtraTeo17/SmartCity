package smartcity.lights.core;

import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import smartcity.config.abstractions.ILightConfigContainer;
import smartcity.stations.ArrivalInfo;
import smartcity.task.data.ISwitchLightsContext;
import utilities.Siblings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.stream.Stream;

import static mocks.TestInstanceCreator.createLights;
import static mocks.TestInstanceCreator.createTimeProvider;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LightSwitcherTests {
    private static final int extendTimeSeconds;
    private static final LocalDateTime currentTime;

    static {
        extendTimeSeconds = 10;
        var date = LocalDate.ofYearDay(2021, 36);
        var time = LocalTime.of(10, 10, 0);
        currentTime = LocalDateTime.of(date, time);
    }

    @Test
    void apply_onEmptyQueue_shouldNotExtend() {
        // Arrange
        var lights = createLights();
        var switcher = createLightSwitcher(lights);
        var context = createContext();

        // Act
        switcher.apply(context);

        // Assert
        assertFalse(context.haveAlreadyExtended());
        var greenLights = lights.first.getLights();
        assertTrue(greenLights.stream().noneMatch(Light::isGreen));
    }

    @Test
    void apply_onStrategyNotActive_shouldNotExtend() {
        // Arrange
        var lights = createLights();
        var greenLights = lights.first.getLights();
        greenLights.forEach(l -> l.carQueue.add("someCar"));

        var switcher = createLightSwitcher(false, lights);
        var context = createContext();

        // Act
        switcher.apply(context);

        // Assert
        assertFalse(context.haveAlreadyExtended());
        assertTrue(greenLights.stream().noneMatch(Light::isGreen));
    }

    @ParameterizedTest
    @MethodSource("testProvider")
    void apply_onStrategyActive_differentCorrectCases_correctResult(String testCaseName,
                                                                    Siblings<SimpleLightGroup> lights,
                                                                    boolean shouldExtend,
                                                                    int expectedExtendSeconds) {
        // Arrange
        var switcher = createLightSwitcher(true, lights);
        var context = createContext();

        // Act
        var extendTime = switcher.apply(context);

        // Assert
        assertEquals(shouldExtend, context.haveAlreadyExtended(), "Invalid extend");
        assertEquals(expectedExtendSeconds * 1000, extendTime, "Invalid extend time");
        var greenLights = lights.first.getLights();
        assertTrue(areLightsCorrect(greenLights, shouldExtend), "Lights should be correct");
    }

    private boolean areLightsCorrect(Collection<? extends Light> lights, boolean isGreen) {
        if (lights.isEmpty()) {
            throw new IllegalArgumentException("Empty light collection");
        }

        var areValid = true;
        Boolean prevGreen = null;
        for (var l : lights) {
            if (prevGreen != null && prevGreen != l.isGreen()) {
                throw new IllegalStateException("Not all lights in group are in the same colour");
            }

            areValid = areValid && (l.isGreen() == isGreen);

            prevGreen = l.isGreen();
        }

        return areValid;
    }


    private static Stream<Arguments> testProvider() {

        // green: (car, ped-red,   farCar, farPed-red)
        // red:   (car, ped-green, farCar, farPed-green)
        return Stream.of(
                arguments("One car, close", prepareLights(
                        1, 0, 0, 0,
                        0, 0, 0, 0
                ), true, extendTimeSeconds),
                arguments("Car vs ped, close", prepareLights(
                        1, 1, 0, 0,
                        0, 0, 0, 0
                ), true, extendTimeSeconds),
                arguments("Red greater, close", prepareLights(
                        1, 1, 0, 0,
                        1, 0, 0, 0
                ), false, extendTimeSeconds),
                arguments("Red equal, close", prepareLights(
                        1, 1, 0, 0,
                        1, 1, 0, 0
                ), false, extendTimeSeconds),
                arguments("Red in close + green in far", prepareLights(
                        0, 1, 1, 1,
                        0, 0, 0, 0
                ), false, extendTimeSeconds),
                arguments("One green, far", prepareLights(
                        0, 0, 1, 0,
                        0, 0, 0, 0
                ), true, extendTimeSeconds),
                arguments("Car vs ped, far", prepareLights(
                        0, 0, 1, 1,
                        0, 0, 0, 0
                ), true, extendTimeSeconds),
                arguments("Red greater, far", prepareLights(
                        0, 0, 1, 1,
                        0, 0, 1, 0
                ), false, extendTimeSeconds),
                arguments("Red equal, far", prepareLights(
                        0, 0, 1, 1,
                        0, 0, 1, 1
                ), false, extendTimeSeconds),
                arguments("Green greater, close", prepareLights(
                        2, 2, 0, 0,
                        1, 1, 0, 0
                ), true, extendTimeSeconds),
                arguments("Green greater, far", prepareLights(
                        0, 0, 3, 3,
                        0, 0, 1, 0
                ), true, extendTimeSeconds),
                arguments("Green greater close and equal far", prepareLights(
                        0, 1, 2, 3,
                        1, 5, 1, 1
                ), true, extendTimeSeconds)
        );
    }

    private static Siblings<SimpleLightGroup> prepareLights(int... objects) {
        var lights = createLights();
        var greenLights = lights.first.getLights();
        prepareLights(greenLights, objects[0], objects[1], objects[2], objects[3]);

        var redLights = lights.second.getLights();
        prepareLights(redLights, objects[4], objects[5], objects[6], objects[7]);

        return lights;
    }

    private static void prepareLights(Collection<? extends Light> lights,
                                      int carsInQueue,
                                      int pedestriansInQueue,
                                      int carsInFarQueue,
                                      int pedestriansInFarQueue) {
        lights.forEach(l -> {
            for (int i = 0; i < carsInQueue; ++i) {
                l.carQueue.add("car" + i);
            }

            for (int i = 0; i < pedestriansInQueue; ++i) {
                l.pedestrianQueue.add("ped" + i);
            }

            for (int i = 0; i < carsInFarQueue; ++i) {
                l.addCarToFarAwayQueue(ArrivalInfo.of("farCar" + i, currentTime.plusSeconds(extendTimeSeconds / 2)));
            }

            for (int i = 0; i < pedestriansInFarQueue; ++i) {
                l.addPedestrianToFarAwayQueue(ArrivalInfo.of("farPed" + i,
                        currentTime.plusSeconds(extendTimeSeconds / 2)));
            }
        });
    }

    @Test
    void apply_onStrategyActive_onNotCorrectObjects_inFarAwayQueue_shouldNotExtend() {
        // Arrange
        var lights = createLights();
        var greenLights = lights.first.getLights();
        greenLights.forEach(l -> l.addCarToFarAwayQueue(ArrivalInfo.of("someCar1",
                currentTime.plusSeconds(extendTimeSeconds + 1))));
        greenLights.forEach(l -> l.addCarToFarAwayQueue(ArrivalInfo.of("someCar2", currentTime.minusSeconds(1))));

        var switcher = createLightSwitcher(lights);
        var context = createContext();

        // Act
        switcher.apply(context);

        // Assert
        assertFalse(context.haveAlreadyExtended());
        assertTrue(greenLights.stream().noneMatch(Light::isGreen));
    }

    @Test
    void apply_onStrategyActive_onCorrectObjects_inFarAwayQueue_shouldExtend_manyTimes() {
        // Arrange
        var lights = createLights();
        var greenLights = lights.first.getLights();
        greenLights.forEach(l -> l.addCarToFarAwayQueue(ArrivalInfo.of("someCar",
                currentTime.plusSeconds(extendTimeSeconds / 2))));

        var switcher = createLightSwitcher(lights);
        var context = createContext();

        // Act & Assert

        // initial green
        switcher.apply(context);
        // green here, extended
        assertTrue(greenLights.stream().allMatch(Light::isGreen));

        switcher.apply(context);
        // green here, extended
        assertTrue(greenLights.stream().allMatch(Light::isGreen));

        switcher.apply(context);
        // green here, extended
        assertTrue(context.haveAlreadyExtended());
        assertTrue(greenLights.stream().allMatch(Light::isGreen));
    }

    @Test
    void apply_onStrategyActive_onNotCorrectObjects_inFarAwayQueue_shouldNotExtend_manyTimes() {
        // Arrange
        var lights = createLights();
        var greenLights = lights.first.getLights();
        greenLights.forEach(l -> l.addCarToFarAwayQueue(ArrivalInfo.of("someCar",
                currentTime.plusSeconds(extendTimeSeconds / 2))));
        var redLights = lights.second.getLights();
        redLights.stream().limit(1).forEach(l -> l.addCarToFarAwayQueue(ArrivalInfo.of("someCar1",
                currentTime.plusSeconds(extendTimeSeconds / 2))));

        var switcher = createLightSwitcher(lights);
        var context = createContext();

        // Act & Assert

        // initial green
        switcher.apply(context);
        // green here, extend initially
        assertTrue(greenLights.stream().allMatch(Light::isGreen));

        switcher.apply(context);
        // red here. not extended again
        assertFalse(context.haveAlreadyExtended());
        assertTrue(greenLights.stream().noneMatch(Light::isGreen));
    }

    private LightSwitcher createLightSwitcher() {
        var lights = createLights();
        return createLightSwitcher(true, lights);
    }

    private LightSwitcher createLightSwitcher(boolean isStrategyActive) {
        return createLightSwitcher(isStrategyActive, createLights());
    }

    private LightSwitcher createLightSwitcher(Siblings<SimpleLightGroup> lights) {
        return createLightSwitcher(true, lights);
    }

    private LightSwitcher createLightSwitcher(boolean isStrategyActive, Siblings<SimpleLightGroup> lights) {
        var configContainer = mock(ILightConfigContainer.class);
        when(configContainer.isLightStrategyActive()).thenReturn(isStrategyActive);

        var timeProvider = createTimeProvider();
        when(timeProvider.getCurrentSimulationTime()).thenReturn(currentTime);

        return new LightSwitcher(timeProvider,
                configContainer, mock(EventBus.class), 1, extendTimeSeconds, lights);
    }

    private static ISwitchLightsContext createContext() {
        return createContext(false);
    }

    private static ISwitchLightsContext createContext(boolean haveAlreadyExtended) {
        return new ISwitchLightsContext() {
            private boolean value = haveAlreadyExtended;

            @Override
            public boolean haveAlreadyExtended() {
                return value;
            }

            @Override
            public void setAlreadyExtendedGreen(boolean value) {
                this.value = value;
            }
        };
    }
}