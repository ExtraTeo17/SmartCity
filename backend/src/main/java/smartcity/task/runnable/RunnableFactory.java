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
        if (separateThread) {
            return new CounterRunnable(getNewThreadExecutor(), action, maxRunCount);
        }

        return new CounterRunnable(executor, action, maxRunCount);
    }

    @Override
    public IFixedExecutionRunnable createWhile(BooleanSupplier test, Runnable runnable, boolean separateThread) {
        if (separateThread) {
            return new WhileRunnable(getNewThreadExecutor(), test, runnable);
        }

        return new WhileRunnable(executor, test, runnable);
    }

    @Override
    public IFixedExecutionRunnable createIf(BooleanSupplier test, Runnable runnable, boolean separateThread) {
         if (separateThread) {
            return new IfRunnable(getNewThreadExecutor(), test, runnable);
        }

        return new IfRunnable(executor, test, runnable);
    }

    @Override
    public IVariableExecutionRunnable createDelay(Supplier<Integer> delayRunnable, int initialDelay, boolean separateThread) {
        if (separateThread) {
            return new InfiniteVariableExecutionRunnable(getNewThreadExecutor(), delayRunnable);
        }
        return new InfiniteVariableExecutionRunnable(executor, delayRunnable);
    }

    private static ScheduledExecutorService getNewThreadExecutor() {
        // TODO: Not sure if performance wise
        return Executors.newSingleThreadScheduledExecutor();
    }
}
