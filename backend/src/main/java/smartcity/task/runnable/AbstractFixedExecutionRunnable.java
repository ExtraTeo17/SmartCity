package smartcity.task.runnable;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

abstract class AbstractFixedExecutionRunnable implements IFixedExecutionRunnable {
    private final ScheduledExecutorService executor;
    volatile ScheduledFuture<?> self;


    protected AbstractFixedExecutionRunnable(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    void onComplete() {
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

    @Override
    public void runFixed(long period, TimeUnit timeUnit) {
        self = executor.scheduleAtFixedRate(this, 0, period, timeUnit);
    }
}
