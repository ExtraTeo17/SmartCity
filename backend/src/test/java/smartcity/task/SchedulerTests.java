package smartcity.task;

import agents.LightManagerAgent;
import agents.abstractions.IAgentsContainer;
import events.SwitchLightsStartEvent;
import events.web.StartSimulationEvent;
import org.junit.jupiter.api.Test;
import smartcity.ITimeProvider;
import smartcity.config.ConfigContainer;
import smartcity.config.ConfigMutator;
import smartcity.lights.LightColor;
import smartcity.task.abstractions.ITaskManager;
import testutils.ReflectionHelper;
import utilities.Siblings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import static mocks.TestInstanceCreator.createEventBus;
import static mocks.TestInstanceCreator.createLightGroup;
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
        var lightsA = createLightGroup(LightColor.RED);
        var lightsB = createLightGroup(LightColor.GREEN);
        var event = new SwitchLightsStartEvent(1, Siblings.of(lightsA, lightsB));

        // Act
        scheduler.handle(event);

        // Assert
        assertTrue(taskScheduled.get());
    }

    private Scheduler createScheduler(ITaskManager taskManager) {
        var configContainer = mock(ConfigContainer.class);
        var agentsContainer = mock(IAgentsContainer.class);
        var timeProvider = mock(ITimeProvider.class);

        return new Scheduler(taskManager, configContainer, agentsContainer, timeProvider, createEventBus());
    }

    @Test
    void handle_StartSimulationEvent_shouldSetAllSettingsInConfigContainer() {
        // Arrange
        ReflectionHelper.setStatic("counter", ConfigMutator.class, 0);
        var configContainer = new ConfigContainer();
        configContainer.setGeneratePedestriansAndBuses(true);
        var ref = new Object() {
            private int carsLimit = 0;
            private int testCarId = 0;

            private int bikesLimit = 0;
            private int testBikeId = 0;
            private int pedLimit = 0;
            private int testPedId = 0;

            private LocalDateTime startTime = null;
            private int timeScale = 0;
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
        doAnswer(invocationOnMock -> {
            ref.startTime = invocationOnMock.getArgument(1);
            return null;
        }).when(taskManager).scheduleSimulationControl(any(BooleanSupplier.class), any(LocalDateTime.class));

        var timeProvider = mock(ITimeProvider.class);
        doAnswer(invocationOnMock -> {
            ref.timeScale = invocationOnMock.getArgument(0);
            return null;
        }).when(timeProvider).setTimeScale(any(int.class));

        var scheduler = createScheduler(configContainer, taskManager, timeProvider);

        var shouldGenerateCars = true;
        var carsNum = 111;
        var testCarId = 112;

        var shouldGenerateBikes = true;
        var bikesNum = 116;
        var testBikeId = 142;

        var pedestriansLimit = 155;
        var testPedestrianId = 555;

        var startTime = LocalDateTime.of(LocalDate.of(2020, 10, 14),
                LocalTime.of(10, 10, 10));
        var timeScale = 12;

        var shouldGenerateBatchesForCars = true;

        var shouldGenerateTP = true;
        var timeBeforeTrouble = 5006;
        var thresholdUntilIndexChange = 101;
        var noTroublePointStrategyIndexFactor = 333;


        var shouldGenerateBusFailures = false;
        var shouldDetectTrafficJams = true;

        var useFixedRoutes = true;
        var useFixedConstructionSites = true;

        var lightStrategyActive = false;
        var extendLightTime = 333;

        var stationStrategyActive = false;
        var extendWaitTime = 354;

        var troublePointStrategyActive = false;
        var trafficJamStrategyActive = false;
        var transportChangeStrategyActive = true;

        var event = new StartSimulationEvent(shouldGenerateCars, carsNum, testCarId, shouldGenerateBatchesForCars,
                shouldGenerateBikes, bikesNum, testBikeId,
                pedestriansLimit, testPedestrianId,
                shouldGenerateTP, timeBeforeTrouble,
                shouldGenerateBusFailures, shouldDetectTrafficJams,
                useFixedRoutes, useFixedConstructionSites,
                startTime, timeScale,
                lightStrategyActive, extendLightTime,
                stationStrategyActive, extendWaitTime,
                troublePointStrategyActive, thresholdUntilIndexChange, noTroublePointStrategyIndexFactor,
                trafficJamStrategyActive, transportChangeStrategyActive
        );

        // Act
        scheduler.handle(event);

        // Assert
        assertEquals(carsNum, ref.carsLimit);
        assertEquals(testCarId, ref.testCarId);

        assertEquals(bikesNum, ref.bikesLimit);
        assertEquals(testBikeId, ref.testBikeId);

        assertEquals(pedestriansLimit, ref.pedLimit);
        assertEquals(testPedestrianId, ref.testPedId);

        assertEquals(ref.startTime, startTime);
        assertEquals(timeScale, ref.timeScale);

        assertEquals(shouldGenerateBatchesForCars, configContainer.shouldGenerateBatchesForCars());

        assertEquals(shouldGenerateTP, configContainer.shouldGenerateConstructionSites());
        assertEquals(timeBeforeTrouble, configContainer.getTimeBeforeTrouble());

        assertEquals(shouldGenerateBusFailures, configContainer.shouldGenerateBusFailures());
        assertEquals(shouldDetectTrafficJams, configContainer.shouldDetectTrafficJams());

        assertEquals(useFixedRoutes, configContainer.shouldUseFixedRoutes());
        assertEquals(useFixedConstructionSites, configContainer.shouldUseFixedConstructionSites());

        assertEquals(lightStrategyActive, configContainer.isLightStrategyActive());
        assertEquals(extendLightTime, configContainer.getExtendLightTime());

        assertEquals(stationStrategyActive, configContainer.isStationStrategyActive());
        assertEquals(extendWaitTime, configContainer.getExtendWaitTime());

        assertEquals(troublePointStrategyActive, configContainer.isConstructionSiteStrategyActive());
        assertEquals(thresholdUntilIndexChange, configContainer.getThresholdUntilIndexChange());
        assertEquals(noTroublePointStrategyIndexFactor, configContainer.getNoConstructionSiteStrategyIndexFactor());

        assertEquals(trafficJamStrategyActive, configContainer.isTrafficJamStrategyActive());
        assertEquals(transportChangeStrategyActive, configContainer.isTransportChangeStrategyActive());
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
        var event = prepareStartEvent(startTime);

        // Act
        scheduler.handle(event);

        // Assert
        assertEquals(startTime, ref.timeSet);
    }

    private Scheduler createScheduler(ConfigContainer configContainer, ITaskManager taskManager, ITimeProvider timeProvider) {
        var agentsContainer = mock(IAgentsContainer.class);
        when(agentsContainer.size(LightManagerAgent.class)).thenReturn(1);
        return new Scheduler(taskManager, configContainer, agentsContainer, timeProvider, createEventBus());
    }

    private StartSimulationEvent prepareStartEvent(LocalDateTime startTime) {
        return new StartSimulationEvent(false, 111, 112,
                false, true, 444,
                222, 5005, 222, true,
                223, false, true,
                false, true, startTime,
                333, false, 354,
                false, 33, false,
                44, 42,
                true, false
        );
    }

}