package smartcity.task.runnable.abstractions;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IRunnableFactory {
    default IFixedExecutionRunnable createCount(Consumer<Integer> action, int maxRunCount) {
        return createCount(action, maxRunCount, false);
    }

    IFixedExecutionRunnable createCount(Consumer<Integer> action, int maxRunCount, boolean separateThread);

    default IFixedExecutionRunnable createWhile(BooleanSupplier test, Runnable runnable) {
        return createWhile(test, runnable, false);
    }

    IFixedExecutionRunnable createWhile(BooleanSupplier test, Runnable runnable, boolean separateThread);

    IFixedExecutionRunnable createIf(BooleanSupplier test, Runnable runnable, boolean separateThread);

    IFixedExecutionRunnable createIf(BooleanSupplier test, Consumer<Integer> countConsumer, boolean separateThread);

    IVariableExecutionRunnable createDelay(Supplier<Integer> delayRunnable, int initialDelay, boolean separateThread);

    default IVariableExecutionRunnable createDelay(Supplier<Integer> delayRunnable, boolean separateThread) {
        return createDelay(delayRunnable, 0, separateThread);
    }

    default IVariableExecutionRunnable createDelay(Supplier<Integer> delayRunnable, int initialDelay) {
        return createDelay(delayRunnable, initialDelay, false);
    }

    default IVariableExecutionRunnable createDelay(Supplier<Integer> delayRunnable) {
        return createDelay(delayRunnable, 0, false);
    }

    List<ScheduledExecutorService> clearAllExecutors();
}
