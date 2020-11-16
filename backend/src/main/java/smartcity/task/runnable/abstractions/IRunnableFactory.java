package smartcity.task.runnable.abstractions;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IRunnableFactory {
    IFixedExecutionRunnable create(Consumer<Integer> action, int maxRunCount);

    IFixedExecutionRunnable create(Consumer<Integer> action, int maxRunCount, boolean separateThread);

    <T> IFixedExecutionRunnable create(BooleanSupplier test, Runnable runnable);

    IVariableExecutionRunnable create(Supplier<Integer> delayRunnable, int initialDelay, boolean separateThread);

    default IVariableExecutionRunnable create(Supplier<Integer> delayRunnable, boolean separateThread) {
        return create(delayRunnable, 0, separateThread);
    }

    default IVariableExecutionRunnable create(Supplier<Integer> delayRunnable, int initialDelay) {
        return create(delayRunnable, initialDelay, false);
    }

    default IVariableExecutionRunnable create(Supplier<Integer> delayRunnable) {
        return create(delayRunnable, 0, false);
    }
}
