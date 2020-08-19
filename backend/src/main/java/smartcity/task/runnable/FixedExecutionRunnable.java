package smartcity.task.runnable;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

// source: https://stackoverflow.com/a/7299823/6841224
class FixedExecutionRunnable implements IFixedExecutionRunnable {
    private final ScheduledExecutorService executor;
    private final AtomicInteger runCount = new AtomicInteger();
    private final Consumer<Integer> action;
    private volatile ScheduledFuture<?> self;
    private final int maxRunCount;

    @Inject
    FixedExecutionRunnable(ScheduledExecutorService executor,
                           @Assisted Consumer<Integer> action,
                           @Assisted int maxRunCount) {
        this.executor = executor;
        this.action = action;
        this.maxRunCount = maxRunCount;
    }

    @Override
    public void run() {
        int count = runCount.incrementAndGet();
        action.accept(count);

        if (count == maxRunCount) {
            boolean interrupted = false;
            try {
                while (self == null) {
                    //noinspection NestedTryStatement
                    try {
                        //noinspection BusyWait
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
                self.cancel(false);
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void runNTimes(long period, TimeUnit timeUnit) {
        self = executor.scheduleAtFixedRate(this, 0, period, timeUnit);
    }
}