package smartcity.task.runnable;

import smartcity.task.runnable.abstractions.ICompletableRunnable;
import smartcity.task.runnable.abstractions.IFixedExecutionRunnable;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// source: https://stackoverflow.com/a/7299823/6841224
abstract class AbstractFixedExecutionRunnable implements IFixedExecutionRunnable, ICompletableRunnable {
    private final ScheduledExecutorService executor;
    private volatile ScheduledFuture<?> self;

    AbstractFixedExecutionRunnable(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public ScheduledFuture<?> getSelf() {
        return self;
    }

    @Override
    public void runFixed(long period, TimeUnit timeUnit) {
        self = executor.scheduleAtFixedRate(this, 0, period, timeUnit);
    }
}
