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
        var switcher = createLightSwitcher();
        var context = createContext();

        // Act
        switcher.apply(context);

        // Assert
        assertTrue(context.haveNotExtendedYet());
    }

    @Test
    void apply_onStrategyNotActive_shouldNotExtend() {
        // Arrange
        var lights = createLights();
        var firstGroup = lights.first;
        firstGroup.getLights().forEach(l -> l.carQueue.add("someCar"));

        var switcher = createLightSwitcher(false, lights);
        var context = createContext();

        // Act
        switcher.apply(context);

        // Assert
        assertTrue(context.haveNotExtendedYet());
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