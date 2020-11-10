package smartcity.task;

import agents.abstractions.IAgentsContainer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import routing.abstractions.IRoutingHelper;
import routing.core.IZone;
import routing.core.Position;
import routing.core.Zone;
import smartcity.task.abstractions.ITaskProvider;
import smartcity.task.runnable.abstractions.IRunnableFactory;
import smartcity.task.runnable.abstractions.IVariableExecutionRunnable;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static mocks.TestInstanceCreator.createLight;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaskManagerTests {

    @Test
    void scheduleSwitchLightTask_shouldStartEndlessTask() {
        // Arrange
        var lights = Arrays.asList(createLight(), createLight(), createLight());
        var taskProvider = mock(ITaskProvider.class);
        Supplier<Integer> switchLightTask = () -> 100;
        when(taskProvider.getSwitchLightsTask(1, lights)).thenReturn(switchLightTask);

        var runnableFactory = mock(IRunnableFactory.class);
        var runContext = new Object() {
            boolean ranOnce = false;
            boolean ranEndless = false;
        };
        when(runnableFactory.createDelay(ArgumentMatchers.<Supplier<Integer>>any(), any(boolean.class))).thenReturn(new IVariableExecutionRunnable() {
            @Override
            public void runOnce(int initialDelay, TimeUnit timeUnit) {
                runContext.ranOnce = true;
            }

            @Override
            public void runEndless(int initialDelay, TimeUnit timeUnit) {
                runContext.ranEndless = true;
            }
        });

        var taskManager = getTaskManager(taskProvider, runnableFactory);

        // Act
        taskManager.scheduleSwitchLightTask(1, lights);

        // Assert
        assertTrue(runContext.ranEndless, "RunEndless should be invoked");
        assertFalse(runContext.ranOnce, "RunOnce should not be invoked");
    }

    private TaskManager getTaskManager(ITaskProvider taskProvider, IRunnableFactory runnableFactory) {
        IAgentsContainer agentsContainer = mock(IAgentsContainer.class);
        IRoutingHelper routingHelper = mock(IRoutingHelper.class);
        IZone zone = Zone.of(Position.of(1, 1), 1);

        return new TaskManager(runnableFactory, agentsContainer, routingHelper, taskProvider, zone);
    }
}