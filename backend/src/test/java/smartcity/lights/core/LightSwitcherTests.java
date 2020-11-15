package smartcity.lights.core;

import com.google.common.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import smartcity.config.abstractions.ILightConfigContainer;
import smartcity.task.data.ISwitchLightsContext;
import utilities.Siblings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static mocks.TestInstanceCreator.createLights;
import static mocks.TestInstanceCreator.createTimeProvider;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LightSwitcherTests {
    private final int extendTimeSeconds;
    private final LocalDateTime currentTime;

    LightSwitcherTests() {
        this.extendTimeSeconds = 10;
        var date = LocalDate.ofYearDay(2021, 36);
        var time = LocalTime.of(10, 10, 0);
        this.currentTime = LocalDateTime.of(date, time);
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
        assertTrue(context.haveNotExtendedYet());
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
        assertTrue(context.haveNotExtendedYet());
        assertTrue(greenLights.stream().noneMatch(Light::isGreen));
    }

    @Test
    void apply_onStrategyActiveAndObjectsInQueue_shouldExtend() {
        // Arrange
        var lights = createLights();
        var greenLights = lights.first.getLights();
        greenLights.forEach(l -> l.carQueue.add("someCar"));

        var switcher = createLightSwitcher(lights);
        var context = createContext();

        // Act
        switcher.apply(context);

        // Assert
        assertFalse(context.haveNotExtendedYet());
        assertTrue(greenLights.stream().allMatch(Light::isGreen));
    }

    @Test
    void apply_onStrategyActive_onCorrectObjects_inFarAwayQueue_shouldExtend() {
        // Arrange
        var lights = createLights();
        var greenLights = lights.first.getLights();
        greenLights.forEach(l -> l.farAwayCarMap.put("someCar", currentTime.plusSeconds(extendTimeSeconds / 2)));

        var switcher = createLightSwitcher(lights);
        var context = createContext();

        // Act
        switcher.apply(context);

        // Assert
        assertFalse(context.haveNotExtendedYet());
        assertTrue(greenLights.stream().allMatch(Light::isGreen));
    }

    @Test
    void apply_onStrategyActive_onNotCorrectObjects_inFarAwayQueue_shouldExtend() {
        // Arrange
        var lights = createLights();
        var greenLights = lights.first.getLights();
        greenLights.forEach(l -> l.farAwayCarMap.put("someCar", currentTime.plusSeconds(extendTimeSeconds + 1)));

        var switcher = createLightSwitcher(lights);
        var context = createContext();

        // Act
        switcher.apply(context);

        // Assert
        assertTrue(context.haveNotExtendedYet());
        assertTrue(greenLights.stream().noneMatch(Light::isGreen));
    }

    @Test
    void apply_onStrategyActive_onNotCorrectObjectsInQueue_onObjectsInFarawayQueue_shouldNotExtend() {
        // Arrange
        var lights = createLights();
        var greenLights = lights.first.getLights();
        greenLights.forEach(l -> l.farAwayCarMap.put("someCar", currentTime.plusSeconds(extendTimeSeconds / 2)));
        var redLights = lights.second.getLights();
        redLights.forEach(l -> l.carQueue.add("someCar1"));

        var switcher = createLightSwitcher(lights);
        var context = createContext();

        // Act
        switcher.apply(context);

        // Assert
        assertTrue(context.haveNotExtendedYet());
        assertTrue(greenLights.stream().noneMatch(Light::isGreen));
    }


    @Test
    void apply_onStrategyActive_onCorrectObjects_inFarAwayQueue_shouldExtend_manyTimes() {
        // Arrange
        var lights = createLights();
        var greenLights = lights.first.getLights();
        greenLights.forEach(l -> l.farAwayCarMap.put("someCar", currentTime.plusSeconds(extendTimeSeconds / 2)));

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
        assertFalse(context.haveNotExtendedYet());
        assertTrue(greenLights.stream().allMatch(Light::isGreen));
    }

    @Test
    void apply_onStrategyActive_onNotCorrectObjects_inFarAwayQueue_shouldNotExtend_manyTimes() {
        // Arrange
        var lights = createLights();
        var greenLights = lights.first.getLights();
        greenLights.forEach(l -> l.farAwayCarMap.put("someCar", currentTime.plusSeconds(extendTimeSeconds / 2)));
        var redLights = lights.second.getLights();
        redLights.stream().limit(1).forEach(l -> l.farAwayCarMap.put("someCar1",
                currentTime.plusSeconds(extendTimeSeconds / 2)));

        var switcher = createLightSwitcher(lights);
        var context = createContext();

        // Act & Assert

        // initial green
        switcher.apply(context);
        // green here, extend initially
        assertTrue(greenLights.stream().allMatch(Light::isGreen));

        switcher.apply(context);
        // red here. not extended again
        assertTrue(context.haveNotExtendedYet());
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
        return createContext(true);
    }

    private static ISwitchLightsContext createContext(boolean wasNotExtended) {
        return new ISwitchLightsContext() {
            private boolean value = wasNotExtended;

            @Override
            public boolean haveNotExtendedYet() {
                return value;
            }

            @Override
            public void setNotExtendedGreen(boolean value) {
                this.value = value;
            }
        };
    }
}