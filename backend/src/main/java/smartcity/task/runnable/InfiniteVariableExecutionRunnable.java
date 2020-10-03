package smartcity.task.runnable;

import smartcity.task.runnable.abstractions.ICompletableRunnable;
import smartcity.task.runnable.abstractions.IVariableExecutionRunnable;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class InfiniteVariableExecutionRunnable implements IVariableExecutionRunnable, ICompletableRunnable {
    private final ScheduledExecutorService executor;
    private final Supplier<Integer> delayRunnable;
    private volatile ScheduledFuture<?> self;

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
        var future = executor.schedule(new Callable<Void>() {
            @Override
            public Void call() {
                if (Thread.interrupted()) {
                    return null;
                }
                int nextDelay = delayRunnable.get();
                if (Thread.interrupted()) {
                    return null;
                }
                self = executor.schedule(this, nextDelay, timeUnit);
                return null;
            }

        }, initialDelay, timeUnit);

        // Beware! - concurrency issues
        if (!future.isDone()) {
            self = future;
        }
    }

    @Override
    public ScheduledFuture<?> getSelf() {
        return self;
    }
}
