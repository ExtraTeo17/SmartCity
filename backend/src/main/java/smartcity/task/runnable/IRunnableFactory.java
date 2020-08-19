package smartcity.task.runnable;

import java.util.function.Consumer;

public interface IRunnableFactory {
    IFixedExecutionRunnable create(Consumer<Integer> action, int maxRunCount);
}
