package smartcity.task.runnable;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutils.ThreadHelper.trySleep;

class InfiniteVariableExecutionRunnableTests {

    @Test
    void runOnce_shouldExecuteOnlyOnce() {
        // Arrange
        var executor = Executors.newSingleThreadScheduledExecutor();
        var initialDelay = 10;
        var delay = 100;
        var delta = delay/2;
        var counter = new AtomicInteger();
        Supplier<Integer> task = () -> {
            counter.getAndIncrement();
            return delay;
        };
        var runnable = new InfiniteVariableExecutionRunnable(executor, task);

        // Act
        runnable.runOnce(initialDelay, TimeUnit.MILLISECONDS);
        trySleep(initialDelay + 2 * delay + delta);

        // Assert
        var executions = counter.get();
        assertEquals(1, executions);
        assertTrue(executor.shutdownNow().isEmpty());
    }

    @Test
    void runOnce_shouldBeRunAfterSpecifiedDelay() {
        // Arrange
        var executor = Executors.newSingleThreadScheduledExecutor();
        var initialDelay = 200;
        var delta = 20;
        var delay = 50;
        var counter = new AtomicInteger();
        Supplier<Integer> task = () -> {
            counter.getAndIncrement();
            return delay;
        };
        var runnable = new InfiniteVariableExecutionRunnable(executor, task);

        // Act
        runnable.runOnce(initialDelay, TimeUnit.MILLISECONDS);
        trySleep(initialDelay / 2);
        var middleExecutions = counter.get();
        trySleep(initialDelay / 2 + delta);
        var finalExecutions = counter.get();

        // Assert
        assertEquals(0, middleExecutions);
        assertEquals(1, finalExecutions);
        assertTrue(executor.shutdownNow().isEmpty());
    }

    @Test
    void runEndless_shouldNotTerminateWithoutExplicitOrder() {
        // Arrange
        var executor = Executors.newSingleThreadScheduledExecutor();
        var initialDelay = 15;
        var delay = 100;
        var counter = new AtomicInteger();
        Supplier<Integer> task = () -> {
            counter.getAndIncrement();
            return delay;
        };
        var runnable = new InfiniteVariableExecutionRunnable(executor, task);
        int expectedRuns = 3;
        int delta = delay / 2;

        // Act
        runnable.runEndless(initialDelay, TimeUnit.MILLISECONDS);
        trySleep(initialDelay + (expectedRuns - 1) * delay + delta);
        runnable.onComplete(true);
        trySleep(delay + delta);

        // Assert
        var executions = counter.get();
        assertEquals(expectedRuns, executions);
        assertTrue(executor.shutdownNow().isEmpty());
    }

    @Test
    void runEndless_shouldBeRanInSpecifiedDelay() {
        // Arrange
        var executor = Executors.newSingleThreadScheduledExecutor();
        int initialDelay = 200;
        int delayMultiplier = 100;
        int initialValue = 1;
        var counter = new AtomicInteger(initialValue);
        Supplier<Integer> task = () -> delayMultiplier * counter.getAndIncrement();
        var runnable = new InfiniteVariableExecutionRunnable(executor, task);
        int expectedRunsWithoutInitial = 3;
        int delta = 15;

        // Act
        runnable.runEndless(initialDelay, TimeUnit.MILLISECONDS);
        trySleep(initialDelay / 2);
        int beforeInitial = counter.get();

        trySleep(initialDelay / 2 + delta);
        int afterInitial = counter.get();

        trySleep(delayMultiplier + delta);
        int after1stDelay = counter.get();

        trySleep(delayMultiplier + delta);
        int before2ndDelay = counter.get();

        // Without delta to not exceed delay
        trySleep(delayMultiplier);
        int after2ndDelay = counter.get();

        trySleep(expectedRunsWithoutInitial * delayMultiplier + delta);
        runnable.onComplete(true);
        trySleep((expectedRunsWithoutInitial + 1) * delayMultiplier);

        // Assert
        var finalExecutions = counter.get();
        assertEquals(initialValue, beforeInitial);
        assertEquals(initialValue + 1, afterInitial);
        assertEquals(initialValue + 2, after1stDelay);
        assertEquals(initialValue + 2, before2ndDelay);
        assertEquals(initialValue + 3, after2ndDelay);
        assertEquals(initialValue + expectedRunsWithoutInitial + 1, finalExecutions);
        assertTrue(executor.shutdownNow().isEmpty());
    }
}