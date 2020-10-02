package smartcity.task.runnable.abstractions;

import java.util.concurrent.TimeUnit;

public interface IVariableExecutionRunnable {
    void runOnce(int initialDelay, TimeUnit timeUnit);

    void runEndless(int initialDelay, TimeUnit timeUnit);
}
