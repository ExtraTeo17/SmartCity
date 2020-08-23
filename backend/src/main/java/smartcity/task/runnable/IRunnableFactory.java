package smartcity.task.runnable;

import com.google.inject.Provides;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface IRunnableFactory {
    @Provides
    IFixedExecutionRunnable create(Consumer<Integer> action, int maxRunCount);

    @Provides
    IFixedExecutionRunnable create(Consumer<Integer> action, int maxRunCount, boolean separateThread);

    @Provides
    <T> IFixedExecutionRunnable create(Predicate<T> predicate, Supplier<T> supplier, Runnable runnable);
}
