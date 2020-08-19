package smartcity.task.runnable;

import java.util.concurrent.TimeUnit;

public interface IFixedExecutionRunnable extends Runnable {
    void runNTimes(long period, TimeUnit timeUnit);
}
