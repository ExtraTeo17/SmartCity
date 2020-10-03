package smartcity.task.runnable.abstractions;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IRunnableFactory {
    IFixedExecutionRunnable create(Consumer<Integer> action, int maxRunCount);

    IFixedExecutionRunnable create(Consumer<Integer> action, int maxRunCount, boolean separateThread);

    <T> IFixedExecutionRunnable create(BooleanSupplier test, Runnable runnable);

    IVariableExecutionRunnable create(Supplier<Integer> delayRunnable, int initialDelay);

    default IVariableExecutionRunnable create(Supplier<Integer> delayRunnable) {
        return create(delayRunnable, 0);
    }
}
