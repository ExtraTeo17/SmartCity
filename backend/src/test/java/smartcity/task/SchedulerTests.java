package smartcity.task;

import agents.abstractions.IAgentsContainer;
import events.SwitchLightsStartEvent;
import events.web.StartSimulationEvent;
import org.junit.jupiter.api.Test;
import smartcity.config.ConfigContainer;
import smartcity.task.abstractions.ITaskManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static mocks.TestInstanceCreator.createEventBus;
import static mocks.TestInstanceCreator.createLight;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@SuppressWarnings("ConstantConditions")
class SchedulerTests {

    @Test
    void handle_switchLightsStartEvent_shouldRunTask() {
        // Arrange
        var taskManager = mock(ITaskManager.class);
        AtomicBoolean taskScheduled = new AtomicBoolean(false);
        doAnswer(invocationOnMock -> {
            taskScheduled.set(true);
            return null;
        }).when(taskManager).scheduleSwitchLightTask(any(int.class), any());
        var scheduler = createScheduler(taskManager);
        var lights = Arrays.asList(createLight(), createLight(), createLight());
        var event = new SwitchLightsStartEvent(1, lights);

        // Act
        scheduler.handle(event);

        // Assert
        assertTrue(taskScheduled.get());
    }

    private Scheduler createScheduler(ITaskManager taskManager) {
        var configContainer = mock(ConfigContainer.class);
        var agentsContainer = mock(IAgentsContainer.class);

        return new Scheduler(taskManager, configContainer, agentsContainer, createEventBus());
    }

    @Test
    void handle_StartSimulationEvent_shouldSetAllSettingsInConfigContainer() {
        // Arrange
        var configContainer = new ConfigContainer();
        configContainer.setGeneratePedestriansAndBuses(true);
        var carsRef = new Object() {
            int carsLimit = 0;
            int testCarId = 0;
        };
        var taskManager = mock(ITaskManager.class);
        doAnswer(invocationOnMock -> {
            carsRef.carsLimit = invocationOnMock.getArgument(0);
            carsRef.testCarId = invocationOnMock.getArgument(1);
            return null;
        }).when(taskManager).scheduleCarCreation(any(int.class), any(int.class));

        var scheduler = createScheduler(configContainer, taskManager);

        var carsNum = 111;
        var testCarId = 112;
        var shouldGenerateCars = true;
        var shouldGenerateTP = true;
        var startTime = LocalDateTime.of(LocalDate.of(2020, 10, 14),
                LocalTime.of(10, 10, 10));
        var lightStrategyActive = false;
        var extendLightTime = 333;
        var stationStrategyActive = false;
        var extendWaitTime = 354;
        var changeRouteStrategyActive = false;

        var event = new StartSimulationEvent(carsNum, testCarId, shouldGenerateCars, shouldGenerateTP, startTime,
                lightStrategyActive, extendLightTime, stationStrategyActive, extendWaitTime, changeRouteStrategyActive);

        // Act
        scheduler.handle(event);

        // Assert
        assertEquals(carsNum, carsRef.carsLimit);
        assertEquals(testCarId, carsRef.testCarId);
        assertEquals(shouldGenerateCars, configContainer.shouldGenerateCars());
        assertEquals(shouldGenerateTP, configContainer.shouldGenerateConstructionSites());
        assertEquals(lightStrategyActive, configContainer.isLightStrategyActive());
        assertEquals(extendLightTime, configContainer.getExtendLightTime());
        assertEquals(stationStrategyActive, configContainer.isStationStrategyActive());
        assertEquals(extendWaitTime, configContainer.getExtendWaitTime());
        assertEquals(changeRouteStrategyActive, configContainer.isChangeRouteStrategyActive());
    }

    @Test
    void handle_StartSimulationEvent_shouldRunSimulationControlTask() {
        // Arrange
        var taskManager = mock(ITaskManager.class);
        var ref = new Object() {
            private LocalDateTime timeSet = null;
        };
        doAnswer(invocationOnMock -> {
            ref.timeSet = invocationOnMock.getArgument(1);
            return null;
        }).when(taskManager).scheduleSimulationControl(any(), any(LocalDateTime.class));
        var scheduler = createScheduler(taskManager);
        var startTime = LocalDateTime.of(LocalDate.of(2020, 10, 14),
                LocalTime.of(10, 10, 10));
        var event = prepareSimulationEvent(startTime);

        // Act
        scheduler.handle(event);

        // Assert
        assertEquals(startTime, ref.timeSet);
    }

    private Scheduler createScheduler(ConfigContainer configContainer, ITaskManager taskManager) {
        var agentsContainer = mock(IAgentsContainer.class);
        return new Scheduler(taskManager, configContainer, agentsContainer, createEventBus());
    }

    private StartSimulationEvent prepareSimulationEvent(LocalDateTime startTime) {
        return new StartSimulationEvent(111, 112, false, true, startTime,
                false, 333, false, 354, false);
    }

}