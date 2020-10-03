package smartcity.task;

import agents.abstractions.IAgentsContainer;
import agents.utilities.LightColor;
import events.SwitchLightsStartEvent;
import org.junit.jupiter.api.Test;
import routing.core.Position;
import smartcity.config.ConfigContainer;
import smartcity.lights.core.Light;
import smartcity.lights.core.LightInfo;
import smartcity.task.abstractions.ITaskManager;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static mocks.TestInstanceCreator.createLight;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class SchedulerTests {

    @Test
    void handle_switchLightsStartEvent_shouldRunTask() {
        // Arrange
        var taskManager = mock(ITaskManager.class);
        AtomicBoolean taskScheduled = new AtomicBoolean(false);
        doAnswer(invocationOnMock -> {
            taskScheduled.set(true);
            return null;
        }).when(taskManager).scheduleSwitchLightTask(any());
        var scheduler = createScheduler(taskManager);
        var lights = Arrays.asList(createLight(), createLight(), createLight());
        var event = new SwitchLightsStartEvent(lights);

        // Act
        scheduler.handle(event);

        // Assert
        assertTrue(taskScheduled.get());
    }

    private Scheduler createScheduler(ITaskManager taskManager) {
        var configContainer = mock(ConfigContainer.class);
        var agentsContainer = mock(IAgentsContainer.class);

        return new Scheduler(taskManager, configContainer, agentsContainer);
    }
}