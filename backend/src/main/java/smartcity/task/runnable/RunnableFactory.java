package smartcity.task.runnable;

import smartcity.task.runnable.abstractions.IFixedExecutionRunnable;
import smartcity.task.runnable.abstractions.IRunnableFactory;
import smartcity.task.runnable.abstractions.IVariableExecutionRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

//TODO: Docs
class RunnableFactory implements IRunnableFactory {
    private ScheduledExecutorService mainExecutor;
    private List<ScheduledExecutorService> executors;

    public RunnableFactory() {
        this.mainExecutor = Executors.newSingleThreadScheduledExecutor();
        this.executors = new ArrayList<>();
        executors.add(mainExecutor);
    }

    @Override
    public IFixedExecutionRunnable createCount(Consumer<Integer> action, int maxRunCount, boolean separateThread) {
        var currentExecutor = separateThread ? getNewThreadExecutor() : mainExecutor;
        return new CounterRunnable(currentExecutor, action, maxRunCount);
    }

    @Override
    public IFixedExecutionRunnable createWhile(BooleanSupplier test, Runnable runnable, boolean separateThread) {
        var currentExecutor = separateThread ? getNewThreadExecutor() : mainExecutor;
        return new WhileRunnable(currentExecutor, test, runnable);
    }

    @Override
    public IFixedExecutionRunnable createIf(BooleanSupplier test, Runnable runnable, boolean separateThread) {
        var currentExecutor = separateThread ? getNewThreadExecutor() : mainExecutor;
        return new IfRunnable(currentExecutor, test, runnable);
    }

    @Override
    public IFixedExecutionRunnable createIf(BooleanSupplier test, Consumer<Integer> countConsumer, boolean separateThread) {
        var currentExecutor = separateThread ? getNewThreadExecutor() : mainExecutor;
        return new IfCounterRunnable(currentExecutor, test, countConsumer);
    }

    @Override
    public IVariableExecutionRunnable createDelay(Supplier<Integer> delayRunnable, int initialDelay, boolean separateThread) {
        var currentExecutor = separateThread ? getNewThreadExecutor() : mainExecutor;
        return new InfiniteVariableExecutionRunnable(currentExecutor, delayRunnable);
    }

    /**
     * @return Old executors
     */
    @Override
    public List<ScheduledExecutorService> clearAllExecutors() {
        var oldExecutors = executors;
        this.mainExecutor = Executors.newSingleThreadScheduledExecutor();
        this.executors = new ArrayList<>();
        this.executors.add(mainExecutor);

        return oldExecutors;
    }

    private ScheduledExecutorService getNewThreadExecutor() {
        var executor = Executors.newSingleThreadScheduledExecutor();
        executors.add(executor);
        return executor;
    }
}
