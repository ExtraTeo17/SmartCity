package smartcity.task.runnable;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

// source: https://stackoverflow.com/a/7299823/6841224
class CounterRunnable extends AbstractFixedExecutionRunnable {
    private final AtomicInteger runCount = new AtomicInteger();
    private final Consumer<Integer> action;
    private final int maxRunCount;

    CounterRunnable(ScheduledExecutorService executor,
                    Consumer<Integer> action,
                    int maxRunCount) {
        super(executor);
        this.action = action;
        this.maxRunCount = maxRunCount;
    }

    @Override
    public void run() {
        int count = runCount.incrementAndGet();
        action.accept(count);

        if (count == maxRunCount) {
            super.onComplete();
        }
    }
}