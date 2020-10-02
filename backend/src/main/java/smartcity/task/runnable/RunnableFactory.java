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
    public IFixedExecutionRunnable create(Consumer<Integer> action, int maxRunCount) {
        return create(action, maxRunCount, false);
    }

    @Override
    public IFixedExecutionRunnable create(Consumer<Integer> action, int maxRunCount, boolean separateThread) {
        if (separateThread) {
            // TODO: Not sure if performance wise
            return new CounterRunnable(Executors.newSingleThreadScheduledExecutor(), action, maxRunCount);
        }

        return new CounterRunnable(executor, action, maxRunCount);
    }

    @Override
    public <T> IFixedExecutionRunnable create(BooleanSupplier test, Runnable runnable) {
        return new TestingRunnable(executor, test, runnable);
    }

    @Override
    public IVariableExecutionRunnable create(Supplier<Integer> delayRunnable, int initialDelay) {
        return new InfiniteVariableExecutionRunnable(executor, delayRunnable);
    }
}
