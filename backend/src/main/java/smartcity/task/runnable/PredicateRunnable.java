package smartcity.task.runnable;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Executes until predicate is true using provided supplier, but at least once
 */
public class PredicateRunnable<T> extends AbstractFixedExecutionRunnable {
    private final Predicate<T> predicate;
    private final Supplier<T> supplier;
    private final Runnable runnable;
    private boolean wasRun;

    PredicateRunnable(ScheduledExecutorService executor,
                      Predicate<T> predicate,
                      Supplier<T> supplier,
                      Runnable runnable) {
        super(executor);
        this.predicate = predicate;
        this.supplier = supplier;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        if (predicate.test(supplier.get())) {
            runnable.run();
            wasRun = true;
        }
        else if (wasRun) {
            super.onComplete();
        }
    }
}
