package smartcity.task;

import agents.LightManagerAgent;
import agents.abstractions.IAgentsContainer;
import events.SwitchLightsStartEvent;
import events.web.StartSimulationEvent;
import org.junit.jupiter.api.Test;
import smartcity.config.ConfigContainer;
import smartcity.config.ConfigMutator;
import smartcity.task.abstractions.ITaskManager;
import testutils.ReflectionHelper;

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
import static org.mockito.Mockito.*;

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
        ReflectionHelper.setStatic("counter", ConfigMutator.class, 0);
        var configContainer = new ConfigContainer();
        configContainer.setGeneratePedestriansAndBuses(true);
        var ref = new Object() {
            int carsLimit = 0;
            int testCarId = 0;
            int bikesLimit = 0;
            int testBikeId = 0;
            int pedLimit = 0;
            int testPedId = 0;
        };
        var taskManager = mock(ITaskManager.class);
        doAnswer(invocationOnMock -> {
            ref.carsLimit = invocationOnMock.getArgument(0);
            ref.testCarId = invocationOnMock.getArgument(1);
            return null;
        }).when(taskManager).scheduleCarCreation(any(int.class), any(int.class));
        doAnswer(invocationOnMock -> {
            ref.pedLimit = invocationOnMock.getArgument(0);
            ref.testPedId = invocationOnMock.getArgument(1);
            return null;
        }).when(taskManager).schedulePedestrianCreation(any(int.class), any(int.class));
        doAnswer(invocationOnMock -> {
            ref.bikesLimit = invocationOnMock.getArgument(0);
            ref.testBikeId = invocationOnMock.getArgument(1);
            return null;
        }).when(taskManager).scheduleBikeCreation(any(int.class), any(int.class));

        var scheduler = createScheduler(configContainer, taskManager);

        var shouldGenerateCars = true;
        var carsNum = 111;
        var testCarId = 112;

        var shouldGenerateBikes = true;
        var bikesNum = 116;
        var testBikeId = 142;

        var shouldGenerateTP = true;
        var shouldGenerateTrafficJams = true;
        var timeBeforeTrouble = 5006;

        int pedestriansLimit = 155;
        int testPedestrianId = 555;

        var startTime = LocalDateTime.of(LocalDate.of(2020, 10, 14),
                LocalTime.of(10, 10, 10));
        var lightStrategyActive = false;
        var extendLightTime = 333;
        var stationStrategyActive = false;
        var extendWaitTime = 354;
        var changeRouteStrategyActive = false;

        var event = new StartSimulationEvent(shouldGenerateCars, carsNum, testCarId,
                shouldGenerateBikes, bikesNum, testBikeId, shouldGenerateTrafficJams,
                shouldGenerateTP, timeBeforeTrouble, pedestriansLimit, testPedestrianId, startTime,
                lightStrategyActive, extendLightTime, stationStrategyActive, extendWaitTime, changeRouteStrategyActive);

        // Act
        scheduler.handle(event);

        // Assert
        assertEquals(carsNum, ref.carsLimit);
        assertEquals(testCarId, ref.testCarId);

        assertEquals(bikesNum, ref.bikesLimit);
        assertEquals(testBikeId, ref.testBikeId);

        assertEquals(shouldGenerateTP, configContainer.shouldGenerateConstructionSites());
        assertEquals(timeBeforeTrouble, configContainer.getTimeBeforeTrouble());
        assertEquals(shouldGenerateTrafficJams, configContainer.shouldGenerateTrafficJams());

        assertEquals(pedestriansLimit, ref.pedLimit);
        assertEquals(testPedestrianId, ref.testPedId);
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
        when(agentsContainer.size(LightManagerAgent.class)).thenReturn(1);
        return new Scheduler(taskManager, configContainer, agentsContainer, createEventBus());
    }

    private StartSimulationEvent prepareSimulationEvent(LocalDateTime startTime) {
        return new StartSimulationEvent(false, 111, 112, false, 444,
                222, false, true,
                5005, 222, 223, startTime,
                false, 333, false, 354, false);
    }

}