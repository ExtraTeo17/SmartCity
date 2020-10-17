package smartcity.task.runnable;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class IfCounterRunnable extends IfRunnable {
    IfCounterRunnable(ScheduledExecutorService executor,
                      BooleanSupplier test,
                      Consumer<Integer> action) {
        super(executor, test, getRunnable(action, new AtomicInteger()));
    }

    private static Runnable getRunnable(Consumer<Integer> action, AtomicInteger counter) {
        return () -> {
            action.accept(counter.incrementAndGet());
        };
    }
}
