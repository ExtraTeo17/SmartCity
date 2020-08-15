package smartcity.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class TaskManager {
    private final ScheduledExecutorService executor;

    public TaskManager() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void runNTimes(Consumer<Integer> runCountConsumer, int maxRunCount, int interval) {
        new FixedExecutionRunnable(runCountConsumer, maxRunCount).runNTimes(interval, TimeUnit.MILLISECONDS);
    }

    // TODO: Add reference to source
    class FixedExecutionRunnable implements Runnable {
        private final AtomicInteger runCount = new AtomicInteger();
        private final Consumer<Integer> action;
        private volatile ScheduledFuture<?> self;
        private final int maxRunCount;

        FixedExecutionRunnable(Consumer<Integer> action, int maxRunCount) {
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

        void runNTimes(long period, TimeUnit unit) {
            self = executor.scheduleAtFixedRate(this, 0, period, unit);
        }
    }
}
