package smartcity.task.runnable;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BooleanSupplier;

/**
 * Executes until predicate is true using provided supplier, but at least once
 */
public class TestingRunnable extends AbstractFixedExecutionRunnable {
    private final BooleanSupplier test;
    private final Runnable runnable;
    private boolean wasRun;

    TestingRunnable(ScheduledExecutorService executor,
                    BooleanSupplier test,
                    Runnable runnable) {
        super(executor);
        this.test = test;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        if (test.getAsBoolean()) {
            runnable.run();
            wasRun = true;
        }
        else if (wasRun) {
            super.onComplete();
        }
    }
}
