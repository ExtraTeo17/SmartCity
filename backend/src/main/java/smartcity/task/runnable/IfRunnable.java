package smartcity.task.runnable;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BooleanSupplier;

public class IfRunnable extends AbstractFixedExecutionRunnable {
    private final BooleanSupplier test;
    private final Runnable runnable;

    IfRunnable(ScheduledExecutorService executor,
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
        }
    }
}
