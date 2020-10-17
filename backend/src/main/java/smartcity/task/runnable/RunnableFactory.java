package smartcity.task.runnable;

import smartcity.task.runnable.abstractions.IFixedExecutionRunnable;
import smartcity.task.runnable.abstractions.IRunnableFactory;
import smartcity.task.runnable.abstractions.IVariableExecutionRunnable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

class RunnableFactory implements IRunnableFactory {
    private final ScheduledExecutorService executor;

    public RunnableFactory() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public IFixedExecutionRunnable createCount(Consumer<Integer> action, int maxRunCount, boolean separateThread) {
        var currentExecutor = separateThread ? getNewThreadExecutor() : executor;
        return new CounterRunnable(currentExecutor, action, maxRunCount);
    }

    @Override
    public IFixedExecutionRunnable createWhile(BooleanSupplier test, Runnable runnable, boolean separateThread) {
        var currentExecutor = separateThread ? getNewThreadExecutor() : executor;
        return new WhileRunnable(currentExecutor, test, runnable);
    }

    @Override
    public IFixedExecutionRunnable createIf(BooleanSupplier test, Runnable runnable, boolean separateThread) {
        var currentExecutor = separateThread ? getNewThreadExecutor() : executor;
        return new IfRunnable(currentExecutor, test, runnable);
    }

    @Override
    public IFixedExecutionRunnable createIf(BooleanSupplier test, Consumer<Integer> countConsumer, boolean separateThread) {
        var currentExecutor = separateThread ? getNewThreadExecutor() : executor;
        return new IfCounterRunnable(currentExecutor, test, countConsumer);
    }

    @Override
    public IVariableExecutionRunnable createDelay(Supplier<Integer> delayRunnable, int initialDelay, boolean separateThread) {
        var currentExecutor = separateThread ? getNewThreadExecutor() : executor;
        return new InfiniteVariableExecutionRunnable(currentExecutor, delayRunnable);
    }

    private static ScheduledExecutorService getNewThreadExecutor() {
        // TODO: Not sure if performance wise
        return Executors.newSingleThreadScheduledExecutor();
    }
}
