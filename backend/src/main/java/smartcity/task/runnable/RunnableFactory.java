package smartcity.task.runnable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

class RunnableFactory implements IRunnableFactory {
    private final ScheduledExecutorService executor;

    public RunnableFactory() {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public IFixedExecutionRunnable create(Consumer<Integer> action, int maxRunCount) {
        return create(action, maxRunCount, false);
    }

    @Override
    public IFixedExecutionRunnable create(Consumer<Integer> action, int maxRunCount, boolean separateThread) {
        if (separateThread) {
            // TODO: Not sure if performance wise
            return new CounterRunnable(Executors.newSingleThreadScheduledExecutor(), action, maxRunCount);
        }

        return new CounterRunnable(executor, action, maxRunCount);
    }

    @Override
    public <T> IFixedExecutionRunnable create(Predicate<T> predicate, Supplier<T> supplier, Runnable runnable) {
        return new PredicateRunnable<T>(executor, predicate, supplier, runnable);
    }
}
