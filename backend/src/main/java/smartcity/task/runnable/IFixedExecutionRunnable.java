package smartcity.task.runnable;

import java.util.concurrent.TimeUnit;

public interface IFixedExecutionRunnable extends Runnable {
    void runFixed(long period, TimeUnit timeUnit);
}
