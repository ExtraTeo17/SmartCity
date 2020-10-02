package smartcity.task.runnable;

import smartcity.task.runnable.abstractions.IVariableExecutionRunnable;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class InfiniteVariableExecutionRunnable implements IVariableExecutionRunnable {
    private final ScheduledExecutorService executor;
    private final Supplier<Integer> delayRunnable;

    InfiniteVariableExecutionRunnable(ScheduledExecutorService executor, Supplier<Integer> runnable) {
        this.executor = executor;
        this.delayRunnable = runnable;
    }

    @Override
    public void runOnce(int initialDelay, TimeUnit timeUnit) {
        executor.schedule(delayRunnable::get, initialDelay, timeUnit);
    }

    @Override
    public void runEndless(int initialDelay, TimeUnit timeUnit) {
        executor.schedule(new Callable<Void>() {
            @Override
            public Void call() {
                int nextDelay = delayRunnable.get();
                executor.schedule(this, nextDelay, timeUnit);
                return null;
            }

        }, initialDelay, timeUnit);
    }
}
